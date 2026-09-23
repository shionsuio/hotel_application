package com.shionsuio.hotel.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
public class ReservationControllerTest {

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUpTestReservations() {
        jdbcTemplate.update("""
                DELETE FROM idempotency_keys
                WHERE idempotency_key LIKE 'reservation-%'
                   OR reservation_id IN (
                       SELECT id
                       FROM reservations
                       WHERE room_id = 1
                         AND check_in_date >= DATE '2099-01-01'
                         AND check_out_date >= DATE '2099-01-01'
                   )
                """);
        jdbcTemplate.update("""
                DELETE FROM reservations
                WHERE room_id = 1
                  AND check_in_date >= DATE '2099-01-01'
                  AND check_out_date >= DATE '2099-01-01'
                """);
    }

    @Test
    @WithMockUser
    @DisplayName("正しい予約リクエストなら予約IDを返す")
    void createReservation() throws Exception {
        mockMvc.perform(
                post("/reservations")
                        .header("Idempotency-Key", "reservation-success-1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                "roomId": 1,
                                "checkInDate": "2099-11-10",
                                "checkOutDate": "2099-11-12"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").isNumber());
    }

    @Test
    @DisplayName("未認証なら予約APIは401を返す")
    void rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(
                post("/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("入力値が不正なら400と共通エラー形式を返す")
    void returnsBadRequestWhenRequestIsInvalid() throws Exception {
        mockMvc.perform(
                post("/reservations")
                        .header("Idempotency-Key", "reservation-invalid-1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("入力値が不正です"));
    }

    @Test
    @WithMockUser
    @DisplayName("予約期間が競合する場合は409を返す")
    void returnsConflictWhenReservationOverlaps() throws Exception {
        jdbcTemplate.update("""
                INSERT INTO reservations
                    (room_id, check_in_date, check_out_date, status)
                VALUES (?, ?, ?, ?)
                """, 1L, java.sql.Date.valueOf("2099-12-10"),
                java.sql.Date.valueOf("2099-12-12"), "CONFIRMED");

        mockMvc.perform(
                post("/reservations")
                        .header("Idempotency-Key", "reservation-conflict-1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomId": 1,
                                  "checkInDate": "2099-12-10",
                                  "checkOutDate": "2099-12-12"
                                }
                                """)
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("RESERVATION_CONFLICT"));
    }

    @Test
    @WithMockUser
    @DisplayName("存在しない部屋なら404を返す")
    void returnsNotFoundWhenRoomDoesNotExist() throws Exception {
        mockMvc.perform(
                post("/reservations")
                        .header("Idempotency-Key", "reservation-not-found-1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomId": 999999,
                                  "checkInDate": "2099-12-10",
                                  "checkOutDate": "2099-12-12"
                                }
                                """)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("ROOM_NOT_FOUND"));
    }

    @Test
    @WithMockUser
    @DisplayName("同じキーと内容の再送は同じ予約結果を返す")
    void returnsSameResultForRetryWithSameKey() throws Exception {
        String request = """
                {
                  "roomId": 1,
                  "checkInDate": "2099-10-10",
                  "checkOutDate": "2099-10-12"
                }
                """;

        MvcResult first = mockMvc.perform(
                post("/reservations")
                        .header("Idempotency-Key", "reservation-retry-1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isOk()).andReturn();

        mockMvc.perform(
                post("/reservations")
                        .header("Idempotency-Key", "reservation-retry-1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isOk())
                .andExpect(content().json(first.getResponse().getContentAsString()));

        Integer idempotencyRows = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM idempotency_keys
                WHERE idempotency_key = ?
                """, Integer.class, "reservation-retry-1");
        Integer reservationRows = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservations
                WHERE room_id = 1
                  AND check_in_date = DATE '2099-10-10'
                  AND check_out_date = DATE '2099-10-12'
                """, Integer.class);

        assertEquals(1, idempotencyRows);
        assertEquals(1, reservationRows);
    }

    @Test
    @WithMockUser
    @DisplayName("冪等性キーとリクエストハッシュをDBに保存する")
    void storesIdempotencyKeyAndRequestHash() throws Exception {
        String key = "reservation-hash-1";
        String request = """
                {
                  "roomId": 1,
                  "checkInDate": "2099-08-10",
                  "checkOutDate": "2099-08-12"
                }
                """;

        mockMvc.perform(
                post("/reservations")
                        .header("Idempotency-Key", key)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isOk());

        String requestHash = jdbcTemplate.queryForObject("""
                SELECT request_hash
                FROM idempotency_keys
                WHERE idempotency_key = ?
                """, String.class, key);

        String expectedHash = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(
                        "1|2099-08-10|2099-08-12".getBytes(StandardCharsets.UTF_8)
                )
        );

        assertEquals(expectedHash, requestHash);
    }

    @Test
    @WithMockUser
    @DisplayName("同じキーで内容が違えば409を返す")
    void rejectsRetryWithDifferentRequest() throws Exception {
        String key = "reservation-retry-different-" + UUID.randomUUID();

        mockMvc.perform(
                post("/reservations")
                        .header("Idempotency-Key", key)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomId": 1,
                                  "checkInDate": "2199-09-10",
                                  "checkOutDate": "2199-09-12"
                                }
                                """)
        ).andExpect(status().isOk());

        mockMvc.perform(
                post("/reservations")
                        .header("Idempotency-Key", key)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomId": 1,
                                  "checkInDate": "2199-09-11",
                                  "checkOutDate": "2199-09-13"
                                }
                                """)
        ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_CONFLICT"));
    }

    @Test
    @WithMockUser
    @DisplayName("存在する予約をキャンセルすると204を返す")
    void cancelsReservation() throws Exception {
        Long reservationId = jdbcTemplate.queryForObject("""
                INSERT INTO reservations
                    (room_id, check_in_date, check_out_date, status)
                VALUES (1, DATE '2099-06-10', DATE '2099-06-12', 'CONFIRMED')
                RETURNING id
                """, Long.class);

        mockMvc.perform(
                delete("/reservations/{reservationId}", reservationId)
                        .with(csrf())
        ).andExpect(status().isNoContent());

        String reservationStatus = jdbcTemplate.queryForObject("""
                SELECT status
                FROM reservations
                WHERE id = ?
                """, String.class, reservationId);
        assertEquals("CANCELED", reservationStatus);
    }

    @Test
    @WithMockUser
    @DisplayName("存在しない予約をキャンセルすると404を返す")
    void returnsNotFoundWhenCancelingMissingReservation() throws Exception {
        mockMvc.perform(
                delete("/reservations/{reservationId}", 999999L)
                        .with(csrf())
        ).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESERVATION_NOT_FOUND"));
    }


}
