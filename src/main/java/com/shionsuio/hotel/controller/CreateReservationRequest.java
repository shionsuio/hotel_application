package com.shionsuio.hotel.controller;

import java.time.LocalDateTime;

public record CreateReservationRequest(
        Long roomId,
        // TODO: userIdはリクエストから除外し、認証情報から予約者を決める（他人名義の予約を防ぐ）。
        String userId,
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime
) {
    // TODO: 必須項目・roomIdの正数・チェックイン < チェックアウト・過去日・最大宿泊期間を検証する。
}
