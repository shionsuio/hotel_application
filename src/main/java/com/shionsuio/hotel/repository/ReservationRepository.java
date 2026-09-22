package com.shionsuio.hotel.repository;

import java.time.LocalDate;

public interface ReservationRepository {
    Long save(Long roomId, LocalDate checkIn, LocalDate checkOut);

    int cancel(Long reservationId);

}
