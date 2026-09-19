package com.shionsuio.hotel.controller;

import jakarta.validation.Validator;
import jakarta.validation.constraints.AssertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class CreateSearchRequestTest {
    @Autowired
    private Validator validator;

    @Test
    @DisplayName("チェックイン日が未指定なら検証エラーになる")
    void rejectsMissingCheckInDate() {
        var request = new CreateSearchRequest(
                null,
                LocalDate.of(2026, 10, 12)
        );

        var violations = validator.validate(request);

        var invalidFields = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
        //違反を順に処理し、違反した項目のパスを取得。パスをcheckInDateという文字列に変換し、集合にまとめている。

        assertEquals(Set.of("checkInDate"), invalidFields);
    }

    @Test
    @DisplayName("チェックアウト日が未指定なら検証エラーになる")
    void rejectMissingCheckOutDate(){
        var request = new CreateSearchRequest(
                LocalDate.of(2026, 10, 10),
                null
        );

        var violations = validator.validate(request);

        var invalidFields = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertEquals(Set.of("checkOutDate"), invalidFields);
    }

    @Test
    @DisplayName("チェックアウト日がチェックイン日より前ならエラーになる")
    void rejectCheckOutBeforeCheckIn(){
        var request = new CreateSearchRequest(
                LocalDate.of(2026, 10,12),
                LocalDate.of(2020, 12, 10)
        );

        var violations = validator.validate(request);

        var invalidFields = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertEquals(Set.of("validPeriod"), invalidFields);
    }

    @Test
    @DisplayName("チェックイン日とチェックアウト日が同じならエラーになる")
    void rejectSameDayStay() {
        var request = new CreateSearchRequest(
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 10)
        );

    var violations = validator.validate(request);

    var invalidFields = violations.stream()
            .map(v -> v.getPropertyPath().toString())
            .collect(Collectors.toSet());

    assertEquals(Set.of("validPeriod"), invalidFields);

    }

    @Test
    @DisplayName("正しい値なら通過する")
    void acceptValidPeriod(){
        var request = new CreateSearchRequest(
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 12)
        );

        var violations = validator.validate(request);

        var invalidFields = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
        //空集合だからエラーがないことになる
        assertEquals(Set.of(), invalidFields);
    }

}
