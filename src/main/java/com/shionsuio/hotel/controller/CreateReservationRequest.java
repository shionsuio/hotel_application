package com.shionsuio.hotel.controller;

import java.time.LocalDateTime;

public record CreateReservationRequest(
        Long roomId,
        String userId,
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime
) {
}
