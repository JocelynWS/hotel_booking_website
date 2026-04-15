package com.hotel;

import com.hotel.model.*;
import com.hotel.service.*;

import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Hotel Booking System - Nhóm 8 ===\n");
        System.out.println("=== Demo Service Layer (Tuần 6) ===\n");

        RoomService roomService = new RoomService();
        GuestService guestService = new GuestService();
        WaitingListService waitingListService = new WaitingListService();
        ReservationService reservationService = new ReservationService(
                roomService, guestService, waitingListService);

        Employee manager = new Employee(
                "EMP001", "Nguyễn Văn A", "0901234567",
                "nva@hotel.com", EmployeeRole.MANAGER, "hashed_pw"
        );
        System.out.println("Nhân viên: " + manager);
        System.out.println("Có thể tạo đặt phòng: " + manager.canCreateReservation());
        System.out.println("Có thể xem báo cáo:   " + manager.canViewReports());

        roomService.addRoom(new Room("R101", "Double", 850_000, 1, 2));
        roomService.addRoom(new Room("R102", "Double", 850_000, 1, 2));
        roomService.addRoom(new Room("R201", "Suite", 1_500_000, 2, 4));
        roomService.addRoom(new Room("R202", "Single", 500_000, 2, 1));
        System.out.println("\n--- Phòng trong hệ thống ---");
        roomService.getAllRooms().forEach(r -> System.out.println("  " + r));
        System.out.println("Phòng trống: " + roomService.countAvailable());

        Guest guest = new Guest(
                "GST001", "Trần Thị B", "0912345678",
                "ttb@email.com", "123 Hà Nội", "079123456789"
        );
        guestService.registerGuest(guest);
        System.out.println("\n--- Khách hàng ---");
        System.out.println("Khách: " + guest);
        System.out.println("Trong blacklist: " + guest.isBlacklisted());

        LocalDate checkIn = LocalDate.now().plusDays(3);
        LocalDate checkOut = LocalDate.now().plusDays(5);
        Room room = roomService.findById("R101").orElseThrow();

        System.out.println("\n--- Tạo đặt phòng ---");
        Result<Reservation> result = reservationService.createReservation(
                guest, room, checkIn, checkOut, 2,
                ReservationType.GUARANTEED, BookingSource.DIRECT,
                "Thẻ tín dụng", "Cần gối êm", manager
        );

        if (result.isSuccess()) {
            Reservation res = result.getData().orElseThrow();
            System.out.println(result.getMessage());
            System.out.println(res);
            System.out.printf("Tổng tiền: %.0f VND%n", res.calculateTotalPrice());
            System.out.printf("Phí hủy:   %.0f VND%n", res.calculateCancellationFee());

            System.out.println("\n--- Xác nhận đặt phòng ---");
            Result<Reservation> confirmResult = reservationService.confirmReservation(
                    res.getReservationId(), manager);
            System.out.println(confirmResult.getMessage());
            System.out.println("Trạng thái: " + res.getStatus());

            System.out.println("\n--- Lịch sử phiếu đặt buồng ---");
            res.getHistoryLog().forEach(h -> System.out.println("  " + h));
        } else {
            System.out.println("Lỗi: " + result.getMessage());
        }

        System.out.println("\n--- Tìm phòng trống ---");
        List<Room> available = reservationService.getAvailableRooms(checkIn, checkOut);
        System.out.println("Phòng trống ngày " + checkIn + " đến " + checkOut + ":");
        available.forEach(r -> System.out.println("  " + r));

        System.out.println("\n--- Danh sách chờ ---");
        Guest guestWaiting = new Guest(
                "GST002", "Lê Văn C", "0987654321",
                "lvc@email.com", "456 HCM", "079999888777"
        );
        waitingListService.addToWaitingList(new WaitingEntry(
                "WAIT-001", guestWaiting, "Double",
                checkIn, checkOut, 2, "Liên hệ trước 9h sáng"
        ));
        System.out.println("Danh sách chờ: " + waitingListService.getAllWaitingEntries());
        System.out.println("Phù hợp phòng R101: " + waitingListService.findMatchingEntries(room));

        System.out.println("\n--- Thống kê đặt phòng ---");
        Map<ReservationStatus, Long> stats = reservationService.getReservationStatistics();
        stats.forEach((status, count) -> System.out.println("  " + status + ": " + count));

        System.out.println("\n=== Demo Service Layer hoàn tất ===");
    }
}
