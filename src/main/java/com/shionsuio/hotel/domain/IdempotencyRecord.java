package com.shionsuio.hotel.domain;

//同じリクエストを送っても結果が１回目と同じになる処理
public record IdempotencyRecord(
        String key,
        String requestHash,
        Long reservationId
) {
}
