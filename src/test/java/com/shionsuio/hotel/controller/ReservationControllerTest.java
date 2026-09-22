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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

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
    }

    @Test
    @WithMockUser
    @DisplayName("同じキーで内容が違えば409を返す")
    void rejectsRetryWithDifferentRequest() throws Exception {
        mockMvc.perform(
                post("/reservations")
                        .header("Idempotency-Key", "reservation-retry-2")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomId": 1,
                                  "checkInDate": "2099-09-10",
                                  "checkOutDate": "2099-09-12"
                                }
                                """)
        ).andExpect(status().isOk());

        mockMvc.perform(
                post("/reservations")
                        .header("Idempotency-Key", "reservation-retry-2")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomId": 1,
                                  "checkInDate": "2099-09-11",
                                  "checkOutDate": "2099-09-13"
                                }
                                """)
        ).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_CONFLICT"));
    }


}
