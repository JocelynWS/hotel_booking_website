package com.hotel.service;

import com.hotel.model.*;
import com.hotel.repository.HotelRepository;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DatabaseManager {

    private static final String FILE_PATH = "database.txt";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final HotelRepository repository;

    public DatabaseManager(HotelRepository repository) {
        this.repository = repository;
    }

    // ═══════════════════════════════════════════════════════════════
    // GHI TOÀN BỘ DỮ LIỆU RA FILE
    // ═══════════════════════════════════════════════════════════════

    public void saveAll() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {

            // ── Ghi danh sách khách ──────────────────────────────
            writer.write("=== GUESTS ===");
            writer.newLine();
            for (Guest g : repository.getAllGuests()) {
                writer.write(String.join("|",
                        nullSafe(g.getGuestId()),
                        nullSafe(g.getFullName()),
                        nullSafe(g.getPhone()),
                        nullSafe(g.getEmail()),
                        nullSafe(g.getAddress()),
                        nullSafe(g.getIdNumber()),
                        String.valueOf(g.isBlacklisted()),
                        nullSafe(g.getBlacklistReason()),
                        g.getCreatedAt() != null ? g.getCreatedAt().format(DATETIME_FMT) : ""
                ));
                writer.newLine();
            }

            // ── Ghi danh sách đặt phòng ──────────────────────────
            writer.write("=== RESERVATIONS ===");
            writer.newLine();
            for (Reservation r : repository.getAllReservations()) {
                writer.write(String.join("|",
                        nullSafe(r.getReservationId()),
                        nullSafe(r.getGuest().getGuestId()),
                        nullSafe(r.getRoom().getRoomId()),
                        r.getCheckInDate().format(DATE_FMT),
                        r.getCheckOutDate().format(DATE_FMT),
                        String.valueOf(r.getNumberOfGuests()),
                        r.getReservationType().name(),
                        r.getStatus().name(),
                        r.getBookingSource().name(),
                        nullSafe(r.getPaymentMethod()),
                        String.valueOf(r.getPricePerNight()),
                        nullSafe(r.getSpecialRequests()),
                        r.getCreatedAt().format(DATETIME_FMT)
                ));
                writer.newLine();
            }

            writer.write("=== END ===");
            System.out.println("✅ Đã lưu dữ liệu vào " + FILE_PATH);

        } catch (IOException e) {
            System.err.println("❌ Lỗi khi ghi file: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ĐỌC DỮ LIỆU TỪ FILE KHI KHỞI ĐỘNG
    // ═══════════════════════════════════════════════════════════════

    public void loadAll() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("ℹ️  Chưa có file database.txt, bắt đầu với dữ liệu mới.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            String section = "";

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("===")) {
                    section = line;
                    continue;
                }
                if (line.isBlank()) continue;

                if (section.contains("GUESTS")) {
                    loadGuest(line);
                } else if (section.contains("RESERVATIONS")) {
                    loadReservation(line);
                }
            }
            System.out.println("✅ Đã tải dữ liệu từ " + FILE_PATH);

        } catch (IOException e) {
            System.err.println("❌ Lỗi khi đọc file: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PARSE TỪNG DÒNG
    // ═══════════════════════════════════════════════════════════════

    private void loadGuest(String line) {
        try {
            String[] p = line.split("\\|", -1);
            Guest g = new Guest(p[0], p[1], p[2], p[3], p[4], p[5]);
            if (Boolean.parseBoolean(p[6])) {
                g.addToBlacklist(p[7]);
            }
            repository.saveGuest(g);
        } catch (Exception e) {
            System.err.println("⚠️  Bỏ qua dòng GUEST lỗi: " + line);
        }
    }

    private void loadReservation(String line) {
        try {
            String[] p = line.split("\\|", -1);
            // Tìm guest và room từ repository
            repository.findGuestById(p[1]).ifPresent(guest ->
                repository.findRoomById(p[2]).ifPresent(room -> {
                    LocalDate checkIn  = LocalDate.parse(p[3], DATE_FMT);
                    LocalDate checkOut = LocalDate.parse(p[4], DATE_FMT);
                    repository.createReservation(
                            guest, room, checkIn, checkOut,
                            Integer.parseInt(p[5]),
                            ReservationType.valueOf(p[6]),
                            BookingSource.valueOf(p[8]),
                            p[9], p[11]
                    );
                })
            );
        } catch (Exception e) {
            System.err.println("⚠️  Bỏ qua dòng RESERVATION lỗi: " + line);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // TIỆN ÍCH
    // ═══════════════════════════════════════════════════════════════

    private String nullSafe(String s) {
        return s != null ? s : "";
    }
}