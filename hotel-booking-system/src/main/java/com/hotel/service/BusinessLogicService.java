package com.hotel.service;

import com.hotel.model.*;
import com.hotel.repository.HotelRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class BusinessLogicService {

    private final HotelRepository repository;

    public BusinessLogicService(HotelRepository repository) {
        this.repository = repository;
    }

    // ═══════════════════════════════════════════════════════════════
    // HÀM CHO MẢNG 1 - Đặt phòng
    // ═══════════════════════════════════════════════════════════════

    /**
     * Kiểm tra SĐT có trong danh sách đen không
     */
    public boolean checkBlacklist(String phone) {
        Optional<Guest> guest = repository.findGuestByPhone(phone);
        return guest.isPresent() && guest.get().isBlacklisted();
    }

    /**
     * Tính 30% tiền cọc
     */
    public double calculateDeposit(double total) {
        return total * 0.30;
    }

    // ═══════════════════════════════════════════════════════════════
    // HÀM CHO MẢNG 2 - Check-in / Check-out
    // ═══════════════════════════════════════════════════════════════

    /**
     * Đánh dấu no-show cho khách
     * Nếu no-show > 2 lần thì tự động blacklist
     */
    public void markNoShow(String reservationId, String employeeId) {
        repository.findReservationById(reservationId).ifPresent(reservation -> {
            // Đánh dấu no-show trên đơn đặt
            reservation.markNoShow(employeeId);
            repository.updateReservation(reservation);

            // Đếm số lần no-show của guest này
            Guest guest = reservation.getGuest();
            long noShowCount = repository.getAllReservations().stream()
                    .filter(r -> r.getGuest().getGuestId().equals(guest.getGuestId()))
                    .filter(r -> r.getStatus() == ReservationStatus.NO_SHOW)
                    .count();

            // Nếu > 2 lần → tự động blacklist
            if (noShowCount > 2) {
                guest.addToBlacklist("No-show quá " + noShowCount + " lần");
                System.out.println("⚠️  Khách " + guest.getFullName()
                        + " đã bị blacklist do no-show " + noShowCount + " lần!");
            }
        });
    }

    /**
     * Tính số tiền còn lại khi check-out (Tổng - Cọc)
     */
    public double calculateFinalBalance(double totalPrice, double deposit) {
        return totalPrice - deposit;
    }

    // ═══════════════════════════════════════════════════════════════
    // THỐNG KÊ & HỖ TRỢ
    // ═══════════════════════════════════════════════════════════════

    /**
     * Lấy danh sách đơn PENDING quá X phút
     */
    public List<Reservation> getExpiredPendingReservations(int minutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);
        return repository.getAllReservations().stream()
                .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                .filter(r -> r.getCreatedAt().isBefore(cutoff))
                .toList();
    }
}