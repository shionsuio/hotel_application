package com.shionsuio.hotel.domain;

public record IdempotencyRecord(
        String key,
        String requestHash,
        Long reservationId
) {
}
