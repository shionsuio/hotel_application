package com.shionsuio.hotel.controller;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateSearchRequest(
        @NotNull(message = "チェックイン日は必須です")
        LocalDate checkInDate,

        @NotNull(message = "チェックアウト日は必須です")
        LocalDate checkOutDate
) {
    // TODO: 日時の必須チェックと期間の前後関係・最大検索期間を検証する。
        @AssertTrue(message = "チェックアウト日はチェックイン日後にしてください")
        public boolean isValidPeriod() {
                //nullの時にtrueを返すのは、未指定の検証を@NotNullに任せるため
                if (checkInDate == null || checkOutDate == null ){
                        return true;
                }

            return checkInDate.isBefore(checkOutDate);
        }

    // TODO: 宿泊期間を日付で扱うか日時で扱うか決め、予約リクエストと統一する。
}
