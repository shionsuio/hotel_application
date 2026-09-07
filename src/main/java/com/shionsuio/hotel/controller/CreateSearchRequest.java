package com.shionsuio.hotel.controller;

import java.time.LocalDateTime;

public record CreateSearchRequest(
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime
) {
}
