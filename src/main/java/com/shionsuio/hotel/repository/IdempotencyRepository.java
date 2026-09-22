package com.shionsuio.hotel.repository;

import com.shionsuio.hotel.domain.IdempotencyRecord;

public interface IdempotencyRepository {
    int insertIfAbsent(String key, String requestHash);

    IdempotencyRecord findByKey(String key);

    void attachReservation(String key, Long reservationId);
}
