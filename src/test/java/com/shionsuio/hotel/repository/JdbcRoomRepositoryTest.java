package com.shionsuio.hotel.repository;

import com.shionsuio.hotel.domain.Room;
import com.shionsuio.hotel.repository.RoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Transactional
public class JdbcRoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 予約がなければ全部屋を取得できる() {
        List<Room> rooms = roomRepository.findAvailableRoom(
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10,12)
        );

        assertEquals(
                List.of(
                        new Room(1L, "101", 10000),
                        new Room(2L, "102", 12000),
                        new Room(3L, "201", 15000)
                ),
                rooms
        );
    }

    @Test
    @DisplayName("予約期間が重なる部屋は取得されない")
    void excludeRoomsWithOverlappingReservations() {
        jdbcTemplate.update("""
                insert into reservations
                    (room_id, check_in_date, check_out_date, status)
                    values (?, ?, ?, ?)
                """,
                1L,
                java.sql.Date.valueOf("2026-10-10"),
                java.sql.Date.valueOf("2026-10-12"),
                "CONFIRMED"
        );

        List<Room> rooms = roomRepository.findAvailableRoom(
                LocalDate.of(2026, 10,11),
                LocalDate.of(2026, 10,13)

        );

        assertEquals(
                List.of(
                        new Room(2L, "102", 12000),
                        new Room(3L, "201", 15000)
                ),
                rooms
        );
    }


    @Test
    @DisplayName("既存予約のチェックアウト当日はチェックインできる")
    void allowCheckInOnExistingCheckOutDate() {
        jdbcTemplate.update("""
                insert into reservations
                    (room_id, check_in_date, check_out_date, status)
                    values (?, ?, ?, ?)
                """,
                1L,
                java.sql.Date.valueOf("2026-10-10"),
                java.sql.Date.valueOf("2026-10-12"),
                "CONFIRMED"
        );

        List<Room> rooms = roomRepository.findAvailableRoom(
                LocalDate.of(2026, 10,12),
                LocalDate.of(2026, 10,14)

        );

        assertEquals(
                List.of(
                        new Room(1L, "101", 10000),
                        new Room(2L, "102", 12000),
                        new Room(3L, "201", 15000)
                ),
                rooms
        );
    }
}
