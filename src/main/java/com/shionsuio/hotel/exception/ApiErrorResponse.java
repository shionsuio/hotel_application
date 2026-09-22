package com.shionsuio.hotel.exception;

public record ApiErrorResponse(
        String code,
        String message
) {
}
