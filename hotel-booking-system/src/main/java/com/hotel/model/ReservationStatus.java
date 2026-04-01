package com.hotel.model;

public enum ReservationStatus {
    PENDING,     // Đang chờ xác nhận
    CONFIRMED,   // Đã xác nhận
    MODIFIED,    // Đã sửa đổi
    CANCELLED,   // Đã hủy
    NO_SHOW,     // Không đến
    CHECKED_IN   // Đã nhận phòng
}
