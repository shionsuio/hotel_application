package com.shionsuio.hotel.repository;

import com.shionsuio.hotel.domain.Room;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomRepository {
    List<Room> findAvailableRoom(
            LocalDateTime checkIn,
            LocalDateTime checkOut
    );
}
