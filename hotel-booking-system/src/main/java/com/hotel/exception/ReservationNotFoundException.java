package com.hotel.exception;

public class ReservationNotFoundException extends HotelException {
    public ReservationNotFoundException(String reservationId) {
        super("Không tìm thấy đặt phòng: " + reservationId);
    }
}
