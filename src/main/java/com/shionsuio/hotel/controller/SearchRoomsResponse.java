package com.shionsuio.hotel.controller;

public record SearchRoomsResponse(
        Long roomId,
        String roomNumber,
        Integer price
) {
}
