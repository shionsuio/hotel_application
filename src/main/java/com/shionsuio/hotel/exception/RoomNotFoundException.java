package com.shionsuio.hotel.exception;

public class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException(Long roomId) {
        super("部屋が見つかりません: " + roomId);
    }
}
