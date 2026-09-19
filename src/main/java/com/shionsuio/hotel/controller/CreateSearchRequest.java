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
        @AssertTrue(message = "チェックアウト日はチェックイン日後にしてください")
        public boolean isValidPeriod() {
                //nullの時にtrueを返すのは、未指定の検証を@NotNullに任せるため
                if (checkInDate == null || checkOutDate == null ){
                        return true;
                }

            return checkInDate.isBefore(checkOutDate);
        }




}
