package com.shionsuio.hotel.exception;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(Long reservationId) {
        super("予約が見つかりません reservationId=" + reservationId);
    }
}
