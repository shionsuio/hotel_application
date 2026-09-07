package com.shionsuio.hotel.controller;

import java.time.LocalDate;

public record CreateReservationRequest(
        Long roomId,
        // TODO: userIdはリクエストから除外し、認証情報から予約者を決める（他人名義の予約を防ぐ）。
        //String userId,
        LocalDate checkInDate,
        LocalDate checkOutDate
) {
    // TODO: 必須項目・roomIdの正数・チェックイン < チェックアウト・過去日・最大宿泊期間を検証する。
}
