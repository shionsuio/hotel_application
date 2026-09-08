package com.shionsuio.hotel.repository;

import com.shionsuio.hotel.domain.Room;
import com.shionsuio.hotel.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Transactional
public class JdbcRoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

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

}
