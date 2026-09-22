package com.shionsuio.hotel.repository;
import com.shionsuio.hotel.repository.JdbcReservationRepository;

import java.time.LocalDate;

public interface ReservationRepository {
    Long save(Long roomId, LocalDate checkIn, LocalDate checkOut);

    int cancel(Long reservationId);

}
