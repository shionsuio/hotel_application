package com.shionsuio.hotel.exception;

public class ReservationConflictException extends RuntimeException{

    public  ReservationConflictException(String message) {
        super(message);
    }
}
