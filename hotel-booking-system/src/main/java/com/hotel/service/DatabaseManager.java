package com.hotel.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.hotel.model.BookingSource;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.ReservationStatus;
import com.hotel.model.ReservationType;
import com.hotel.model.Room;
import com.hotel.model.RoomStatus;
import com.hotel.repository.HotelRepository;

@Component
public class DatabaseManager {

    private final JdbcTemplate jdbcTemplate;
    private final HotelRepository repository;

    @Autowired
    public DatabaseManager(JdbcTemplate jdbcTemplate, HotelRepository repository) {
        this.jdbcTemplate = jdbcTemplate;
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        createTables();
        loadAll();
    }

    // ═══════════════════════════════════════════════════════════════
    // KHỞI TẠO BẢNG CSDL (SCHEMA)
    // ═══════════════════════════════════════════════════════════════

    public void createTables() {
        System.out.println("====== KHỞI TẠO CẤU TRÚC BẢNG CSDL SQL (H2) ======");
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS rooms (" +
                "room_id VARCHAR(50) PRIMARY KEY, " +
                "room_type VARCHAR(100) NOT NULL, " +
                "price_per_night DOUBLE NOT NULL, " +
                "floor INT NOT NULL, " +
                "capacity INT NOT NULL, " +
                "status VARCHAR(50) NOT NULL, " +
                "description VARCHAR(255)" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS guests (" +
                "guest_id VARCHAR(50) PRIMARY KEY, " +
                "full_name VARCHAR(255) NOT NULL, " +
                "phone VARCHAR(50) NOT NULL, " +
                "email VARCHAR(100), " +
                "address VARCHAR(255), " +
                "id_number VARCHAR(50), " +
                "fax VARCHAR(50), " +
                "registrant_name VARCHAR(255), " +
                "password VARCHAR(255), " +
                "blacklisted BOOLEAN DEFAULT FALSE, " +
                "blacklist_reason VARCHAR(255), " +
                "blacklisted_at TIMESTAMP, " +
                "created_at TIMESTAMP NOT NULL" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS reservations (" +
                "reservation_id VARCHAR(50) PRIMARY KEY, " +
                "guest_id VARCHAR(50) NOT NULL, " +
                "room_id VARCHAR(50) NOT NULL, " +
                "check_in_date DATE NOT NULL, " +
                "check_out_date DATE NOT NULL, " +
                "number_of_guests INT NOT NULL, " +
                "reservation_type VARCHAR(50) NOT NULL, " +
                "status VARCHAR(50) NOT NULL, " +
                "booking_source VARCHAR(50) NOT NULL, " +
                "payment_method VARCHAR(255), " +
                "price_per_night DOUBLE NOT NULL, " +
                "special_requests VARCHAR(255), " +
                "cancel_reason VARCHAR(255), " +
                "created_at TIMESTAMP NOT NULL, " +
                "created_by VARCHAR(50) NOT NULL, " +
                "FOREIGN KEY (guest_id) REFERENCES guests(guest_id), " +
                "FOREIGN KEY (room_id) REFERENCES rooms(room_id)" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS reservation_history (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "reservation_id VARCHAR(50) NOT NULL, " +
                "action VARCHAR(50) NOT NULL, " +
                "description VARCHAR(255) NOT NULL, " +
                "employee_id VARCHAR(50) NOT NULL, " +
                "timestamp TIMESTAMP NOT NULL, " +
                "FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE CASCADE" +
                ")");
    }

    // ═══════════════════════════════════════════════════════════════
    // TẢI DỮ LIỆU TỪ CSDL VÀO BỘ NHỚ
    // ═══════════════════════════════════════════════════════════════

    public void loadAll() {
        try {
            System.out.println("====== TẢI DỮ LIỆU TỪ CSDL H2 ======");

            // 1. Tải danh sách phòng
            Integer roomCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM rooms", Integer.class);
            if (roomCount == null || roomCount == 0) {
                System.out.println(" CSDL chưa có phòng nào. Đang nạp danh sách phòng mẫu vào CSDL...");
                // Lưu phòng mẫu từ bộ nhớ vào CSDL
                for (Room r : repository.getAllRooms()) {
                    saveRoom(r);
                }
            } else {
                // Đã có phòng trong CSDL -> Xóa phòng mẫu trong repository và tải từ CSDL
                repository.clearRooms();
                List<Room> rooms = jdbcTemplate.query("SELECT * FROM rooms", this::mapRowToRoom);
                for (Room r : rooms) {
                    repository.addRoom(r);
                }
                System.out.println(" Đã tải " + rooms.size() + " phòng từ CSDL SQL.");
            }

            // 2. Tải danh sách khách hàng
            repository.clearAll(); // dọn sạch guests, reservations trong repo trước khi load
            // Nạp lại phòng để chắc chắn repo đồng bộ phòng
            List<Room> currentRooms = jdbcTemplate.query("SELECT * FROM rooms", this::mapRowToRoom);
            repository.clearRooms();
            for (Room r : currentRooms) {
                repository.addRoom(r);
            }

            List<Guest> guests = jdbcTemplate.query("SELECT * FROM guests", this::mapRowToGuest);
            int maxGuestNum = 0;
            for (Guest g : guests) {
                repository.saveGuest(g);
                String id = g.getGuestId();
                if (id != null && id.startsWith("GST")) {
                    try {
                        int num = Integer.parseInt(id.substring(3));
                        if (num > maxGuestNum) maxGuestNum = num;
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }
            repository.setGuestCounter(maxGuestNum + 1);
            System.out.println(" Đã tải " + guests.size() + " khách hàng từ CSDL SQL. Thiết lập bộ đếm khách: " + (maxGuestNum + 1));

            // 3. Tải danh sách đơn đặt phòng
            List<Reservation> reservations = jdbcTemplate.query("SELECT * FROM reservations", this::mapRowToReservation);
            int maxResNum = 0;
            for (Reservation r : reservations) {
                // Tải lịch sử cho từng đơn
                List<Reservation.ReservationHistory> history = jdbcTemplate.query(
                        "SELECT * FROM reservation_history WHERE reservation_id = ? ORDER BY timestamp ASC",
                        (rs, rowNum) -> new Reservation.ReservationHistory(
                                rs.getString("action"),
                                rs.getString("description"),
                                rs.getString("employee_id"),
                                rs.getTimestamp("timestamp").toLocalDateTime()
                        ),
                        r.getReservationId()
                );
                r.setHistoryLog(history);
                repository.updateReservation(r); // vì hàm mapRowToReservation đã tự đăng ký vào list, nhưng update lại history
                
                String id = r.getReservationId();
                if (id != null && id.startsWith("RES")) {
                    try {
                        int num = Integer.parseInt(id.substring(3));
                        if (num > maxResNum) maxResNum = num;
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }
            repository.setReservationCounter(maxResNum + 1);
            System.out.println(" Đã tải " + reservations.size() + " đơn đặt phòng từ CSDL SQL. Thiết lập bộ đếm đơn: " + (maxResNum + 1));

        } catch (Exception e) {
            System.err.println(" Lỗi khi tải dữ liệu từ CSDL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // LƯU TRỮ TỪNG THỰC THỂ (WRITE-THROUGH TO SQL)
    // ═══════════════════════════════════════════════════════════════

    public void saveRoom(Room r) {
        String sql = "MERGE INTO rooms KEY(room_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                r.getRoomId(),
                r.getRoomType(),
                r.getPricePerNight(),
                r.getFloor(),
                r.getCapacity(),
                r.getStatus().name(),
                r.getDescription()
        );
    }

    public void saveGuest(Guest g) {
        String sql = "MERGE INTO guests KEY(guest_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                g.getGuestId(),
                g.getFullName(),
                g.getPhone(),
                g.getEmail(),
                g.getAddress(),
                g.getIdNumber(),
                g.getFax(),
                g.getRegistrantName(),
                g.getPassword(),
                g.isBlacklisted(),
                g.getBlacklistReason(),
                g.getBlacklistedAt() != null ? java.sql.Timestamp.valueOf(g.getBlacklistedAt()) : null,
                java.sql.Timestamp.valueOf(g.getCreatedAt())
        );
    }

    public void saveReservation(Reservation r) {
        // 1. Lưu thông tin đơn đặt
        String sql = "MERGE INTO reservations KEY(reservation_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                r.getReservationId(),
                r.getGuest().getGuestId(),
                r.getRoom().getRoomId(),
                java.sql.Date.valueOf(r.getCheckInDate()),
                java.sql.Date.valueOf(r.getCheckOutDate()),
                r.getNumberOfGuests(),
                r.getReservationType().name(),
                r.getStatus().name(),
                r.getBookingSource().name(),
                r.getPaymentMethod(),
                r.getPricePerNight(),
                r.getSpecialRequests(),
                r.getCancelReason(),
                java.sql.Timestamp.valueOf(r.getCreatedAt()),
                r.getCreatedBy()
        );

        // 2. Đồng bộ lịch sử đơn đặt
        jdbcTemplate.update("DELETE FROM reservation_history WHERE reservation_id = ?", r.getReservationId());
        for (Reservation.ReservationHistory hist : r.getHistoryLog()) {
            jdbcTemplate.update("INSERT INTO reservation_history (reservation_id, action, description, employee_id, timestamp) VALUES (?, ?, ?, ?, ?)",
                    r.getReservationId(),
                    hist.getAction(),
                    hist.getDescription(),
                    hist.getEmployeeId(),
                    java.sql.Timestamp.valueOf(hist.getTimestamp())
            );
        }
    }

    public void saveAll() {
        System.out.println(" Đang ghi nhận đồng bộ toàn bộ dữ liệu xuống CSDL SQL H2...");
        try {
            for (Room r : repository.getAllRooms()) {
                saveRoom(r);
            }
            for (Guest g : repository.getAllGuests()) {
                saveGuest(g);
            }
            for (Reservation r : repository.getAllReservations()) {
                saveReservation(r);
            }
            System.out.println(" Đồng bộ dữ liệu hoàn tất.");
        } catch (Exception e) {
            System.err.println(" Lỗi khi lưu dữ liệu hàng loạt: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MAPPING HELPER METHODS
    // ═══════════════════════════════════════════════════════════════

    private Room mapRowToRoom(ResultSet rs, int rowNum) throws SQLException {
        Room r = new Room(
                rs.getString("room_id"),
                rs.getString("room_type"),
                rs.getDouble("price_per_night"),
                rs.getInt("floor"),
                rs.getInt("capacity")
        );
        r.setStatus(RoomStatus.valueOf(rs.getString("status")));
        r.setDescription(rs.getString("description"));
        return r;
    }

    private Guest mapRowToGuest(ResultSet rs, int rowNum) throws SQLException {
        Guest g = new Guest(
                rs.getString("guest_id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address"),
                rs.getString("id_number")
        );
        g.setFax(rs.getString("fax"));
        g.setRegistrantName(rs.getString("registrant_name"));
        g.setPassword(rs.getString("password"));
        if (rs.getBoolean("blacklisted")) {
            g.addToBlacklist(rs.getString("blacklist_reason"));
        }
        return g;
    }

    private Reservation mapRowToReservation(ResultSet rs, int rowNum) throws SQLException {
        String guestId = rs.getString("guest_id");
        String roomId = rs.getString("room_id");

        Guest guest = repository.findGuestById(guestId)
                .orElseGet(() -> {
                    System.err.println(" Không tìm thấy khách hàng ID: " + guestId + " cho đơn đặt phòng!");
                    return new Guest(guestId, "Unknown Guest", "", "", "", "");
                });

        Room room = repository.findRoomById(roomId)
                .orElseGet(() -> {
                    System.err.println(" Không tìm thấy phòng ID: " + roomId + " cho đơn đặt phòng!");
                    return new Room(roomId, "Standard", 0, 1, 2);
                });

        Reservation r = new Reservation(
                rs.getString("reservation_id"),
                guest,
                room,
                rs.getDate("check_in_date").toLocalDate(),
                rs.getDate("check_out_date").toLocalDate(),
                rs.getInt("number_of_guests"),
                ReservationType.valueOf(rs.getString("reservation_type")),
                BookingSource.valueOf(rs.getString("booking_source")),
                rs.getString("payment_method"),
                rs.getString("created_by")
        );
        r.setStatus(ReservationStatus.valueOf(rs.getString("status")));
        r.setPricePerNight(rs.getDouble("price_per_night"));
        r.setSpecialRequests(rs.getString("special_requests"));
        
        // Cập nhật cancelReason trực tiếp nếu có
        String cancelReason = rs.getString("cancel_reason");
        if (cancelReason != null && !cancelReason.isEmpty() && r.getStatus() == ReservationStatus.CANCELLED) {
            r.cancel(cancelReason, rs.getString("created_by"));
        }

        return r;
    }
}