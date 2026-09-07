package com.shionsuio.hotel.controller;

import java.time.LocalDate;

public record CreateSearchRequest(
        LocalDate checkInDate,
        LocalDate checkOutDate
) {
    // TODO: 日時の必須チェックと期間の前後関係・最大検索期間を検証する。
    // TODO: 宿泊期間を日付で扱うか日時で扱うか決め、予約リクエストと統一する。
}
