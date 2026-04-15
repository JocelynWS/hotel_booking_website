package com.hotel.exception;

public class GuestBlacklistedException extends HotelException {
    public GuestBlacklistedException(String guestId, String reason) {
        super("Khách " + guestId + " nằm trong danh sách đen: " + reason);
    }
}
