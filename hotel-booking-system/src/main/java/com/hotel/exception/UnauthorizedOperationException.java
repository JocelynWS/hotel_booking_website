package com.hotel.exception;

public class UnauthorizedOperationException extends HotelException {
    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
