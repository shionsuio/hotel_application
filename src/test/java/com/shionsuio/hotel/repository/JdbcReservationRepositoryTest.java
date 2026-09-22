package com.shionsuio.hotel.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class JdbcReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;



    @Test
    @DisplayName("save()を読んでIDが取得できるのかテスト")
    void saveReservationReturnsGeneratedId() {
        Long reservationId = reservationRepository.save(
                1L,
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10,12)
        );

        assertTrue(reservationId > 0);
    }

    @Test
    @DisplayName("予約をキャンセルするとステータスがCANCELEDになる")
    void cancelsReservation() {
        Long reservationId = reservationRepository.save(
                1L,
                LocalDate.of(2026, 10,10),
                LocalDate.of(2026, 10,12)
        );

        int updateRows = reservationRepository.cancel(
                reservationId
        );

        assertEquals(1, updateRows);

        String status = jdbcTemplate.queryForObject(
                "select status from reservations where id = ?;",
                String.class,
                reservationId
        );

        assertEquals("CANCELED", status);
    }
}
