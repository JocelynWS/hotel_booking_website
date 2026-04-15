package com.hotel.repository;

import com.hotel.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DatabaseService {

    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final EmployeeRepository employeeRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingEntryRepository waitingEntryRepository;

    public DatabaseService() {
        this.roomRepository = new RoomRepository();
        this.guestRepository = new GuestRepository();
        this.employeeRepository = new EmployeeRepository();
        this.reservationRepository = new ReservationRepository();
        this.waitingEntryRepository = new WaitingEntryRepository();
    }

    public void initializeSampleData() {
        if (roomRepository.count() == 0) {
            initializeRooms();
        }
        if (employeeRepository.count() == 0) {
            initializeEmployees();
        }
    }

    private void initializeRooms() {
        List<Room> rooms = List.of(
            new Room("R101", "Single", 500_000, 1, 1),
            new Room("R102", "Single", 500_000, 1, 1),
            new Room("R103", "Double", 850_000, 1, 2),
            new Room("R104", "Double", 850_000, 1, 2),
            new Room("R201", "Triple", 1_200_000, 2, 3),
            new Room("R202", "Suite", 1_500_000, 2, 4),
            new Room("R301", "VIP Suite", 2_500_000, 3, 6)
        );
        rooms.forEach(room -> room.setDescription("Phòng " + room.getRoomType() + " tầng " + room.getFloor()));
        roomRepository.saveAll(rooms);
    }

    private void initializeEmployees() {
        List<Employee> employees = List.of(
            new Employee("EMP001", "Nguyễn Văn A", "0901234567", "nva@hotel.com", EmployeeRole.MANAGER, "password123"),
            new Employee("EMP002", "Trần Thị B", "0912345678", "ttb@hotel.com", EmployeeRole.RESERVATION_STAFF, "password123"),
            new Employee("EMP003", "Lê Văn C", "0923456789", "lvc@hotel.com", EmployeeRole.RECEPTIONIST, "password123")
        );
        employeeRepository.saveAll(employees);
    }

    public Optional<Room> findRoomById(String roomId) {
        return roomRepository.findById(roomId);
    }

    public List<Room> findAvailableRooms() {
        return roomRepository.findAvailable();
    }

    public List<Room> findRoomsByType(String roomType) {
        return roomRepository.findByType(roomType);
    }

    public List<Room> findAvailableRooms(String roomType, int minCapacity, LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.findAvailable().stream()
                .filter(r -> r.getRoomType().equalsIgnoreCase(roomType))
                .filter(r -> r.getCapacity() >= minCapacity)
                .filter(r -> !isRoomBooked(r.getRoomId(), checkIn, checkOut))
                .toList();
    }

    public boolean isRoomBooked(String roomId, LocalDate checkIn, LocalDate checkOut) {
        return !reservationRepository.findActiveReservations(roomId, checkIn, checkOut).isEmpty();
    }

    public void saveRoom(Room room) {
        if (roomRepository.exists(room.getRoomId())) {
            roomRepository.update(room);
        } else {
            roomRepository.save(room);
        }
    }

    public void updateRoomStatus(String roomId, RoomStatus status) {
        roomRepository.findById(roomId).ifPresent(room -> {
            room.setStatus(status);
            roomRepository.update(room);
        });
    }

    public Optional<Guest> findGuestById(String guestId) {
        return guestRepository.findById(guestId);
    }

    public Optional<Guest> findGuestByPhone(String phone) {
        return guestRepository.findByPhone(phone);
    }

    public void saveGuest(Guest guest) {
        if (guestRepository.exists(guest.getGuestId())) {
            guestRepository.update(guest);
        } else {
            guestRepository.save(guest);
        }
    }

    public List<Guest> findBlacklistedGuests() {
        return guestRepository.findBlacklisted();
    }

    public Optional<Employee> findEmployeeById(String employeeId) {
        return employeeRepository.findById(employeeId);
    }

    public Optional<Employee> login(String email, String password) {
        return employeeRepository.findByEmail(email)
                .filter(e -> e.getPasswordHash().equals(password) && e.isActive());
    }

    public List<Employee> findEmployeesByRole(EmployeeRole role) {
        return employeeRepository.findByRole(role);
    }

    public void saveReservation(Reservation reservation) {
        if (reservationRepository.exists(reservation.getReservationId())) {
            reservationRepository.update(reservation);
        } else {
            reservationRepository.save(reservation);
        }
    }

    public Optional<Reservation> findReservationById(String reservationId) {
        return reservationRepository.findById(reservationId);
    }

    public List<Reservation> findReservationsByGuest(String guestId) {
        return reservationRepository.findByGuestId(guestId);
    }

    public List<Reservation> findReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status);
    }

    public List<Reservation> findReservationsByDateRange(LocalDate startDate, LocalDate endDate) {
        return reservationRepository.findByDateRange(startDate, endDate);
    }

    public List<Reservation> findTodayCheckIns() {
        return reservationRepository.findByCheckInDate(LocalDate.now());
    }

    public List<Reservation> findTodayCheckOuts() {
        return reservationRepository.findByCheckOutDate(LocalDate.now());
    }

    public void saveWaitingEntry(WaitingEntry entry) {
        if (waitingEntryRepository.exists(entry.getWaitingId())) {
            waitingEntryRepository.update(entry);
        } else {
            waitingEntryRepository.save(entry);
        }
    }

    public Optional<WaitingEntry> findWaitingEntryById(String waitingId) {
        return waitingEntryRepository.findById(waitingId);
    }

    public List<WaitingEntry> findWaitingEntriesByRoomType(String roomType) {
        return waitingEntryRepository.findByRoomType(roomType);
    }

    public List<WaitingEntry> findMatchingWaitingEntries(Room room, LocalDate date) {
        return waitingEntryRepository.findNotNotified().stream()
                .filter(w -> w.getRoomType().equalsIgnoreCase(room.getRoomType()))
                .filter(w -> w.getNumberOfGuests() <= room.getCapacity())
                .filter(w -> !w.getDesiredCheckIn().isAfter(date))
                .toList();
    }

    public void deleteWaitingEntry(String waitingId) {
        waitingEntryRepository.delete(waitingId);
    }

    public void cleanExpiredWaitingEntries() {
        waitingEntryRepository.removeExpiredEntries(LocalDate.now().minusDays(1));
    }

    public long countAvailableRooms() {
        return roomRepository.findAvailable().size();
    }

    public long countTotalRooms() {
        return roomRepository.count();
    }

    public long countActiveReservations() {
        return reservationRepository.findByStatus(ReservationStatus.CONFIRMED).stream().count() +
               reservationRepository.findByStatus(ReservationStatus.CHECKED_IN).stream().count();
    }

    public double calculateOccupancyRate() {
        long total = roomRepository.count();
        if (total == 0) return 0;
        long occupied = roomRepository.findByStatus(RoomStatus.OCCUPIED).size();
        return (double) occupied / total * 100;
    }

    public RoomRepository getRoomRepository() { return roomRepository; }
    public GuestRepository getGuestRepository() { return guestRepository; }
    public EmployeeRepository getEmployeeRepository() { return employeeRepository; }
    public ReservationRepository getReservationRepository() { return reservationRepository; }
    public WaitingEntryRepository getWaitingEntryRepository() { return waitingEntryRepository; }
}
