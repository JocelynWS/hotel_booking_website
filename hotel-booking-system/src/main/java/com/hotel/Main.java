package com.hotel;

import com.hotel.model.*;
import com.hotel.repository.DatabaseService;

import java.time.LocalDate;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Hotel Booking System - Nhóm 8 ===\n");
        System.out.println("=== Demo Repository Layer (Tuần 7) ===\n");

        DatabaseService db = new DatabaseService();
        db.initializeSampleData();

        System.out.println("--- Danh sách phòng (từ file JSON) ---");
        List<Room> rooms = db.findAvailableRooms();
        rooms.forEach(r -> System.out.printf("  %s: %s - %.0f VND/đêm%n", 
                r.getRoomId(), r.getRoomType(), r.getPricePerNight()));
        System.out.printf("Tổng phòng: %d | Phòng trống: %d%n%n", 
                db.countTotalRooms(), db.countAvailableRooms());

        System.out.println("--- Nhân viên (từ file JSON) ---");
        db.findEmployeesByRole(EmployeeRole.MANAGER).forEach(e -> 
                System.out.println("  " + e));

        System.out.println("\n--- Tạo đặt phòng mới ---");
        Room room = db.findRoomById("R103").orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));
        Guest guest = new Guest("GST001", "Trần Thị B", "0912345678", 
                "ttb@email.com", "123 Hà Nội", "079123456789");
        db.saveGuest(guest);

        Employee emp = db.findEmployeesByRole(EmployeeRole.RESERVATION_STAFF)
                .stream().findFirst().orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        LocalDate checkIn = LocalDate.now().plusDays(3);
        LocalDate checkOut = LocalDate.now().plusDays(5);

        Reservation reservation = new Reservation(
                "RES-001", guest, room, checkIn, checkOut, 2,
                ReservationType.GUARANTEED, BookingSource.DIRECT,
                "Thẻ tín dụng", emp.getEmployeeId()
        );
        reservation.confirm(emp.getEmployeeId());
        db.saveReservation(reservation);
        db.updateRoomStatus(room.getRoomId(), RoomStatus.OCCUPIED);

        System.out.println("Đặt phòng: " + reservation.getReservationId());
        System.out.println("Khách: " + guest.getFullName());
        System.out.println("Phòng: " + room.getRoomId());
        System.out.printf("Tổng tiền: %.0f VND%n%n", reservation.calculateTotalPrice());

        System.out.println("--- Check-in/Check-out hôm nay ---");
        db.findTodayCheckIns().forEach(r -> System.out.println("  Check-in: " + r.getReservationId()));
        db.findTodayCheckOuts().forEach(r -> System.out.println("  Check-out: " + r.getReservationId()));

        System.out.println("\n--- Thống kê ---");
        System.out.printf("Tỷ lệ lấp đầy: %.1f%%%n", db.calculateOccupancyRate());
        System.out.println("Phòng trống: " + db.countAvailableRooms());
        System.out.println("Đặt phòng đang hoạt động: " + db.findReservationsByStatus(ReservationStatus.CONFIRMED).size());

        System.out.println("\n--- File data đã được tạo trong thư mục /data ---");
        System.out.println("=== Demo Repository Layer hoàn tất ===");
    }
}
