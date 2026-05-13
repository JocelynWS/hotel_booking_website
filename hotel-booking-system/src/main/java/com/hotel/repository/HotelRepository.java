package com.hotel.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.hotel.model.BookingSource;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.ReservationStatus;
import com.hotel.model.ReservationType;
import com.hotel.model.Room;
import com.hotel.model.RoomStatus;

public class HotelRepository {

    private final List<Room> rooms = new ArrayList<>();
    private final List<Guest> guests = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();
    private int guestCounter = 1;
    private int reservationCounter = 1;

    public HotelRepository() {
        initializeSampleData();
    }

    private void initializeSampleData() {
        // Deluxe City View - 770.000 VNĐ/đêm - 2 người
        rooms.add(new Room("D101", "Deluxe City View", 770_000, 1, 2));
        rooms.add(new Room("D102", "Deluxe City View", 770_000, 1, 2));
        rooms.add(new Room("D103", "Deluxe City View", 770_000, 1, 2));
        rooms.add(new Room("D104", "Deluxe City View", 770_000, 1, 2));
        rooms.add(new Room("D105", "Deluxe City View", 770_000, 1, 2));

        // Deluxe Triple City View - 990.000 VNĐ/đêm - 3 người
        rooms.add(new Room("DT101", "Deluxe Triple City View", 990_000, 1, 3));
        rooms.add(new Room("DT102", "Deluxe Triple City View", 990_000, 1, 3));
        rooms.add(new Room("DT103", "Deluxe Triple City View", 990_000, 1, 3));

        // Executive Suite City View - 1.210.000 VNĐ/đêm - 2 người
        rooms.add(new Room("E101", "Executive Suite City View", 1_210_000, 2, 2));
        rooms.add(new Room("E102", "Executive Suite City View", 1_210_000, 2, 2));
        rooms.add(new Room("E103", "Executive Suite City View", 1_210_000, 2, 2));

        // Executive Twin City View - 1.430.000 VNĐ/đêm - 3 người
        rooms.add(new Room("ET101", "Executive Twin City View", 1_430_000, 2, 3));
        rooms.add(new Room("ET102", "Executive Twin City View", 1_430_000, 2, 3));

        // Presidential Suite - 3.905.000 VNĐ/đêm - 2 người
        rooms.add(new Room("P101", "Presidential Suite", 3_905_000, 3, 2));
        rooms.add(new Room("P102", "Presidential Suite", 3_905_000, 3, 2));
    }

    // ═══════════════════════════════════════════════════════════════
    // ROOM OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms);
    }

    public Optional<Room> findRoomById(String roomId) {
        return rooms.stream()
                .filter(r -> r.getRoomId().equalsIgnoreCase(roomId))
                .findFirst();
    }

    public List<Room> findAvailableRooms() {
        return rooms.stream()
                .filter(Room::isAvailable)
                .toList();
    }

    public List<Room> findRoomsByType(String roomType) {
        return rooms.stream()
                .filter(r -> r.getRoomType().equalsIgnoreCase(roomType))
                .toList();
    }

    /**
     * BƯỚC 2: Xác định khả năng đáp ứng
     * Tính số buồng có thể bán:
     * = Tổng buồng − buồng không dùng được − buồng đang có khách − buồng đã đặt trước
     *   + buồng đặt không chắc + buồng mới huỷ + buồng trả sớm hơn dự định
     */
    public RoomAvailability analyzeAvailability(LocalDate checkIn, LocalDate checkOut) {
        int totalRooms = rooms.size();
        
        int unavailable = (int) rooms.stream()
                .filter(r -> r.getStatus() == RoomStatus.MAINTENANCE || 
                            r.getStatus() == RoomStatus.OUT_OF_ORDER)
                .count();
        
        int currentlyOccupied = (int) rooms.stream()
                .filter(r -> r.getStatus() == RoomStatus.OCCUPIED)
                .count();
        
        int bookedInPeriod = (int) reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED ||
                            r.getStatus() == ReservationStatus.PENDING)
                .filter(r -> hasDateOverlap(r.getCheckInDate(), r.getCheckOutDate(), checkIn, checkOut))
                .count();
        
        int uncertainBookings = (int) reservations.stream()
                .filter(r -> r.getReservationType() == ReservationType.NON_GUARANTEED)
                .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                .filter(r -> hasDateOverlap(r.getCheckInDate(), r.getCheckOutDate(), checkIn, checkOut))
                .count();
        
        int roomsAvailable = totalRooms - unavailable - currentlyOccupied - bookedInPeriod + uncertainBookings;
        
        return new RoomAvailability(totalRooms, unavailable, currentlyOccupied, 
                                   bookedInPeriod, uncertainBookings, roomsAvailable);
    }

    private boolean hasDateOverlap(LocalDate r1Start, LocalDate r1End, LocalDate r2Start, LocalDate r2End) {
        return !r1End.isBefore(r2Start) && !r1Start.isAfter(r2End.minusDays(1));
    }

    public List<Room> findAvailableRoomsForPeriod(String roomType, int guests, LocalDate checkIn, LocalDate checkOut) {
        return rooms.stream()
                .filter(r -> r.getRoomType().equalsIgnoreCase(roomType))
                .filter(r -> r.getCapacity() >= guests)
                .filter(r -> r.getStatus() == RoomStatus.AVAILABLE)
                .filter(r -> !isRoomBookedInPeriod(r.getRoomId(), checkIn, checkOut))
                .toList();
    }

    public boolean isRoomBookedInPeriod(String roomId, LocalDate checkIn, LocalDate checkOut) {
        return reservations.stream()
                .filter(r -> r.getRoom().getRoomId().equals(roomId))
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED || 
                            r.getStatus() == ReservationStatus.PENDING)
                .anyMatch(r -> hasDateOverlap(r.getCheckInDate(), r.getCheckOutDate(), checkIn, checkOut));
    }

    public void updateRoomStatus(String roomId, RoomStatus status) {
        findRoomById(roomId).ifPresent(r -> r.setStatus(status));
    }

    // ═══════════════════════════════════════════════════════════════
    // GUEST OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    public Guest saveGuest(Guest guest) {
        if (guest.getGuestId() == null || guest.getGuestId().isEmpty()) {
            guest.setGuestId("GST" + String.format("%04d", guestCounter++));
        }
        guests.add(guest);
        return guest;
    }

    public Optional<Guest> findGuestById(String guestId) {
        return guests.stream()
                .filter(g -> g.getGuestId().equals(guestId))
                .findFirst();
    }

    public Optional<Guest> findGuestByPhone(String phone) {
        return guests.stream()
                .filter(g -> g.getPhone().equals(phone))
                .findFirst();
    }

    public List<Guest> getAllGuests() {
        return new ArrayList<>(guests);
    }

    // ═══════════════════════════════════════════════════════════════
    // RESERVATION OPERATIONS
    // ═══════════════════════════════════════════════════════════════

    public Reservation createReservation(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut,
                                          int numberOfGuests, ReservationType type, BookingSource source,
                                          String paymentMethod, String specialRequests) {
        String reservationId = "RES" + String.format("%06d", reservationCounter++);
        
        Reservation reservation = new Reservation(
                reservationId, guest, room, checkIn, checkOut, numberOfGuests, type, source, paymentMethod
        );
        reservation.setSpecialRequests(specialRequests);
        reservation.setStatus(ReservationStatus.PENDING);
        
        reservations.add(reservation);
        return reservation;
    }

    public Optional<Reservation> findReservationById(String reservationId) {
        return reservations.stream()
                .filter(r -> r.getReservationId().equals(reservationId))
                .findFirst();
    }

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }

    public List<Reservation> findReservationsByDate(LocalDate date) {
        return reservations.stream()
                .filter(r -> r.getCheckInDate().equals(date))
                .toList();
    }

    public void updateReservation(Reservation reservation) {
        int index = -1;
        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).getReservationId().equals(reservation.getReservationId())) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            reservations.set(index, reservation);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // STATISTICS
    // ═══════════════════════════════════════════════════════════════

    public int countTotalRooms() { return rooms.size(); }
    
    public int countAvailableRooms() {
        return (int) rooms.stream().filter(Room::isAvailable).count();
    }

    public int countOccupiedRooms() {
        return (int) rooms.stream()
                .filter(r -> r.getStatus() == RoomStatus.OCCUPIED)
                .count();
    }

    public int countUnavailableRooms() {
        return (int) rooms.stream()
                .filter(r -> r.getStatus() == RoomStatus.MAINTENANCE || 
                            r.getStatus() == RoomStatus.OUT_OF_ORDER)
                .count();
    }

    public double getOccupancyRate() {
        int totalUsable = rooms.size() - countUnavailableRooms();
        if (totalUsable == 0) return 0;
        return (double) countOccupiedRooms() / totalUsable * 100;
    }

    // Inner class for availability analysis
    public record RoomAvailability(
        int totalRooms,
        int unavailableRooms,
        int currentlyOccupied,
        int bookedInPeriod,
        int uncertainBookings,
        int roomsAvailable
    ) {}
}
