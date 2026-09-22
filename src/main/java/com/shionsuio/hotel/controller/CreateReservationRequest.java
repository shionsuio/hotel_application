package com.shionsuio.hotel.controller;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateReservationRequest(
        @NotNull(message = "部屋IDは必須です")
        @Positive(message = "部屋IDは正の値で指定してください")
        Long roomId,

        @NotNull(message = "チェックイン日は必須です")
        LocalDate checkInDate,
        @NotNull(message = "チェックアウト日は必須です")
        LocalDate checkOutDate
) {
    // TODO: 必須項目・roomIdの正数・チェックイン < チェックアウト・過去日・最大宿泊期間を検証する。
    @AssertTrue(message = "チェックアウト日はチェックイン日より前にしてください")
    public boolean isValidPeriod() {
        if(checkInDate == null || checkOutDate == null) {
            return true;
        }

        return checkInDate.isBefore(checkOutDate);
    }
}
