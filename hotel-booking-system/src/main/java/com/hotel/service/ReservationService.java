package com.hotel.service;

import com.hotel.exception.*;
import com.hotel.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReservationService {

    private final List<Reservation> reservations;
    private final RoomService roomService;
    private final GuestService guestService;
    private final WaitingListService waitingListService;

    private int reservationCounter;

    public ReservationService() {
        this.reservations = new ArrayList<>();
        this.roomService = new RoomService();
        this.guestService = new GuestService();
        this.waitingListService = new WaitingListService();
        this.reservationCounter = 1;
    }

    public ReservationService(RoomService roomService, GuestService guestService, WaitingListService waitingListService) {
        this.reservations = new ArrayList<>();
        this.roomService = roomService;
        this.guestService = guestService;
        this.waitingListService = waitingListService;
        this.reservationCounter = 1;
    }

    private String generateReservationId() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        return String.format("RES-%s-%03d", date, reservationCounter++);
    }

    public Result<Reservation> createReservation(
            Guest guest,
            Room room,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int numberOfGuests,
            ReservationType reservationType,
            BookingSource bookingSource,
            String paymentMethod,
            String specialRequests,
            Employee createdBy) {

        if (guest == null) return Result.fail("Thông tin khách hàng không được null");
        if (room == null) return Result.fail("Thông tin phòng không được null");
        if (checkInDate == null || checkOutDate == null) return Result.fail("Ngày check-in/check-out không được null");
        if (checkInDate.isAfter(checkOutDate)) return Result.fail("Ngày check-out phải sau check-in");
        if (checkInDate.isBefore(LocalDate.now())) return Result.fail("Ngày check-in không thể trong quá khứ");
        if (createdBy == null || !createdBy.canCreateReservation()) {
            return Result.fail("Nhân viên không có quyền tạo đặt phòng");
        }

        if (guestService.findById(guest.getGuestId()).isEmpty()) {
            guestService.registerGuest(guest);
        }

        try {
            guestService.validateGuest(guest);
        } catch (GuestBlacklistedException e) {
            return Result.fail(e.getMessage());
        }

        if (!isRoomAvailableForPeriod(room.getRoomId(), checkInDate, checkOutDate)) {
            List<Room> alternatives = roomService.findAvailableByTypeAndCapacity(
                    room.getRoomType(), numberOfGuests);
            
            if (!alternatives.isEmpty()) {
                return Result.fail(alternatives, 
                        "Phòng " + room.getRoomId() + " đã được đặt. Gợi ý: " + 
                        alternatives.stream().map(Room::getRoomId).collect(Collectors.joining(", ")));
            }
            return Result.fail("Phòng đã hết. Bạn có muốn thêm vào danh sách chờ không?");
        }

        String reservationId = generateReservationId();
        Reservation reservation = new Reservation(
                reservationId, guest, room,
                checkInDate, checkOutDate, numberOfGuests,
                reservationType, bookingSource, paymentMethod,
                createdBy.getEmployeeId()
        );
        reservation.setSpecialRequests(specialRequests);

        reservations.add(reservation);
        roomService.markAsOccupied(room.getRoomId());

        return Result.ok(reservation, "Tạo đặt phòng thành công. Mã: " + reservationId);
    }

    public Result<Reservation> createReservation(
            Guest guest, Room room, LocalDate checkIn, LocalDate checkOut,
            int numberOfGuests, ReservationType type, BookingSource source,
            String paymentMethod, String createdBy) {
        
        Employee emp = new Employee("TEMP", "Temp", "0", "temp@temp.com", 
                EmployeeRole.RESERVATION_STAFF, "x");
        emp.setEmployeeId(createdBy);
        return createReservation(guest, room, checkIn, checkOut, numberOfGuests, 
                type, source, paymentMethod, null, emp);
    }

    public Result<Reservation> confirmReservation(String reservationId, Employee employee) {
        if (!employee.canCreateReservation()) {
            return Result.fail("Nhân viên không có quyền xác nhận đặt phòng");
        }

        Reservation reservation = findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            return Result.fail("Chỉ có thể xác nhận đặt phòng đang ở trạng thái PENDING");
        }

        reservation.confirm(employee.getEmployeeId());
        return Result.ok(reservation, "Xác nhận đặt phòng thành công");
    }

    public Result<Reservation> cancelReservation(String reservationId, String reason, Employee employee) {
        if (!employee.canCancelReservation()) {
            return Result.fail("Nhân viên không có quyền hủy đặt phòng");
        }

        Reservation reservation = findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return Result.fail("Đặt phòng đã bị hủy trước đó");
        }

        if (reservation.getStatus() == ReservationStatus.CHECKED_IN) {
            return Result.fail("Không thể hủy phòng đã nhận");
        }

        double cancellationFee = reservation.calculateCancellationFee();
        reservation.cancel(reason, employee.getEmployeeId());
        
        roomService.markAsCleaning(reservation.getRoom().getRoomId());
        
        notifyMatchingWaitingList(reservation.getRoom(), reservation.getCheckInDate());

        String message = cancellationFee > 0 
                ? String.format("Hủy đặt phòng thành công. Phí hủy: %.0f VND", cancellationFee)
                : "Hủy đặt phòng thành công";

        return Result.ok(reservation, message);
    }

    public Result<Reservation> modifyReservation(
            String reservationId,
            Room newRoom,
            LocalDate newCheckIn,
            LocalDate newCheckOut,
            int newNumberOfGuests,
            String changeDetail,
            Employee employee) {

        if (!employee.canModifyReservation()) {
            return Result.fail("Nhân viên không có quyền sửa đặt phòng");
        }

        Reservation reservation = findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (reservation.getStatus() == ReservationStatus.CANCELLED ||
            reservation.getStatus() == ReservationStatus.CHECKED_IN) {
            return Result.fail("Không thể sửa đặt phòng đã hủy hoặc đã nhận phòng");
        }

        Room oldRoom = reservation.getRoom();
        boolean roomChanged = newRoom != null && !newRoom.getRoomId().equals(oldRoom.getRoomId());

        if (roomChanged) {
            if (!isRoomAvailableForPeriod(newRoom.getRoomId(), newCheckIn, newCheckOut)) {
                return Result.fail("Phòng mới không khả dụng trong khoảng thời gian này");
            }
            roomService.markAsAvailable(oldRoom.getRoomId());
            reservation.setRoom(newRoom);
            roomService.markAsOccupied(newRoom.getRoomId());
        }

        if (newCheckIn != null) reservation.setCheckInDate(newCheckIn);
        if (newCheckOut != null) reservation.setCheckOutDate(newCheckOut);
        if (newNumberOfGuests > 0) reservation.setNumberOfGuests(newNumberOfGuests);

        reservation.modify(changeDetail, employee.getEmployeeId());

        return Result.ok(reservation, "Cập nhật đặt phòng thành công");
    }

    public Result<Reservation> checkIn(String reservationId, Employee employee) {
        Reservation reservation = findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED &&
            reservation.getStatus() != ReservationStatus.PENDING) {
            return Result.fail("Chỉ có thể check-in đặt phòng đã xác nhận hoặc đang chờ");
        }

        if (!reservation.getCheckInDate().equals(LocalDate.now()) &&
            !reservation.getCheckInDate().isBefore(LocalDate.now())) {
            return Result.fail("Chưa đến ngày check-in");
        }

        reservation.confirm(employee.getEmployeeId());
        roomService.markAsOccupied(reservation.getRoom().getRoomId());

        return Result.ok(reservation, "Check-in thành công");
    }

    public Result<Reservation> checkOut(String reservationId, Employee employee) {
        Reservation reservation = findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            return Result.fail("Chỉ có thể check-out phòng đã nhận");
        }

        roomService.markAsCleaning(reservation.getRoom().getRoomId());
        reservation.cancel("Check-out", employee.getEmployeeId());

        return Result.ok(reservation, "Check-out thành công. Tổng tiền: " + 
                String.format("%.0f VND", reservation.calculateTotalPrice()));
    }

    public Result<Reservation> markNoShow(String reservationId, Employee employee) {
        Reservation reservation = findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED &&
            reservation.getStatus() != ReservationStatus.PENDING) {
            return Result.fail("Trạng thái không phù hợp để đánh dấu no-show");
        }

        reservation.markNoShow(employee.getEmployeeId());
        roomService.markAsAvailable(reservation.getRoom().getRoomId());
        notifyMatchingWaitingList(reservation.getRoom(), reservation.getCheckInDate());

        return Result.ok(reservation, "Đánh dấu no-show thành công");
    }

    public boolean isRoomAvailableForPeriod(String roomId, LocalDate checkIn, LocalDate checkOut) {
        return reservations.stream()
                .filter(r -> r.getRoom().getRoomId().equals(roomId))
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .filter(r -> r.getStatus() != ReservationStatus.NO_SHOW)
                .noneMatch(r -> 
                        !(checkOut.isBefore(r.getCheckInDate()) || checkIn.isAfter(r.getCheckOutDate().minusDays(1))));
    }

    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut, String roomType, int numberOfGuests) {
        return roomService.findAvailableByTypeAndCapacity(roomType, numberOfGuests).stream()
                .filter(room -> isRoomAvailableForPeriod(room.getRoomId(), checkIn, checkOut))
                .toList();
    }

    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        return roomService.findAvailable().stream()
                .filter(room -> isRoomAvailableForPeriod(room.getRoomId(), checkIn, checkOut))
                .toList();
    }

    public void notifyMatchingWaitingList(Room room, LocalDate date) {
        List<WaitingEntry> matches = waitingListService.findMatchingEntries(room);
        for (WaitingEntry entry : matches) {
            if (entry.getDesiredCheckIn().equals(date) || entry.getDesiredCheckIn().isAfter(date)) {
                entry.markNotified();
            }
        }
    }

    public Optional<Reservation> findById(String reservationId) {
        return reservations.stream()
                .filter(r -> r.getReservationId().equals(reservationId))
                .findFirst();
    }

    public List<Reservation> findByGuestId(String guestId) {
        return reservations.stream()
                .filter(r -> r.getGuest().getGuestId().equals(guestId))
                .toList();
    }

    public List<Reservation> findByStatus(ReservationStatus status) {
        return reservations.stream()
                .filter(r -> r.getStatus() == status)
                .toList();
    }

    public List<Reservation> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return reservations.stream()
                .filter(r -> !(r.getCheckOutDate().isBefore(startDate) || r.getCheckInDate().isAfter(endDate)))
                .toList();
    }

    public List<Reservation> findByCheckInDate(LocalDate date) {
        return reservations.stream()
                .filter(r -> r.getCheckInDate().equals(date))
                .toList();
    }

    public List<Reservation> findByCheckOutDate(LocalDate date) {
        return reservations.stream()
                .filter(r -> r.getCheckOutDate().equals(date))
                .toList();
    }

    public List<Reservation> findByBookingSource(BookingSource source) {
        return reservations.stream()
                .filter(r -> r.getBookingSource() == source)
                .toList();
    }

    public double calculateTotalRevenue(LocalDate startDate, LocalDate endDate) {
        return findByDateRange(startDate, endDate).stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .mapToDouble(Reservation::calculateTotalPrice)
                .sum();
    }

    public double calculateTotalRevenue() {
        return calculateTotalRevenue(LocalDate.now().withDayOfYear(1), LocalDate.now());
    }

    public Map<ReservationStatus, Long> getReservationStatistics() {
        return reservations.stream()
                .collect(Collectors.groupingBy(Reservation::getStatus, Collectors.counting()));
    }

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }

    public RoomService getRoomService() { return roomService; }
    public GuestService getGuestService() { return guestService; }
    public WaitingListService getWaitingListService() { return waitingListService; }
}
