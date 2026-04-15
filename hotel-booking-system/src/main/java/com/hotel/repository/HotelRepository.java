package com.hotel.repository;

import com.hotel.model.*;

import java.time.LocalDate;
import java.util.*;

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
        rooms.add(new Room("R101", "Single", 500_000, 1, 1));
        rooms.add(new Room("R102", "Single", 500_000, 1, 1));
        rooms.add(new Room("R103", "Double", 850_000, 1, 2));
        rooms.add(new Room("R104", "Double", 850_000, 1, 2));
        rooms.add(new Room("R105", "Double", 900_000, 1, 2));
        rooms.add(new Room("R201", "Triple", 1_200_000, 2, 3));
        rooms.add(new Room("R202", "Triple", 1_200_000, 2, 3));
        rooms.add(new Room("R203", "Suite", 1_500_000, 2, 4));
        rooms.add(new Room("R204", "Suite", 1_500_000, 2, 4));
        rooms.add(new Room("R301", "VIP Suite", 2_500_000, 3, 6));
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
