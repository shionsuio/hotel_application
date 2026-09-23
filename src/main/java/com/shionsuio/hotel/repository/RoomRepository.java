package com.shionsuio.hotel.repository;

import com.shionsuio.hotel.domain.Room;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository {
    List<Room> findAvailableRoom(
            LocalDate checkIn,
            LocalDate checkOut
    );

    Room findByIdForUpdate(Long roomId);


}
