package com.hotel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Reservation {

    // ── Thông tin cơ bản ─────────────────────────────────────────────────────
    private String            reservationId;     // Số phiếu (tự động)
    private Guest             guest;
    private String            registrantName;    // Tên người đăng ký (nếu khác khách)
    private Room              room;
    private int               numberOfGuests;

    // ── Thời gian ────────────────────────────────────────────────────────────
    private LocalDate         checkInDate;
    private LocalDate         checkOutDate;
    private int               numberOfNights;    // Tự tính

    // ── Phân loại & Trạng thái ───────────────────────────────────────────────
    private ReservationType   reservationType;
    private ReservationStatus status;
    private BookingSource     bookingSource;

    // ── Tài chính ────────────────────────────────────────────────────────────
    private double            pricePerNight;
    private String            paymentMethod;

    // ── Yêu cầu đặc biệt & Ghi chú ─────────────────────────────────────────
    private String            specialRequests;
    private String            cancelReason;

    // ── Lịch sử thao tác ────────────────────────────────────────────────────
    private List<ReservationHistory> historyLog;

    private LocalDateTime     createdAt;
    private String            createdBy;        // employeeId

    // ── Constructor ──────────────────────────────────────────────────────────

    public Reservation(String reservationId, Guest guest, Room room,
                       LocalDate checkInDate, LocalDate checkOutDate,
                       int numberOfGuests, ReservationType reservationType,
                       BookingSource bookingSource, String paymentMethod,
                       String createdBy) {
        this.reservationId   = reservationId;
        this.guest           = guest;
        this.room            = room;
        this.checkInDate     = checkInDate;
        this.checkOutDate    = checkOutDate;
        this.numberOfGuests  = numberOfGuests;
        this.reservationType = reservationType;
        this.bookingSource   = bookingSource;
        this.paymentMethod   = paymentMethod;
        this.createdBy       = createdBy;
        this.status          = ReservationStatus.PENDING;
        this.pricePerNight   = room.getPricePerNight();
        this.historyLog      = new ArrayList<>();
        this.createdAt       = LocalDateTime.now();

        // Tự tính số đêm
        this.numberOfNights = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);

        // Ghi lịch sử tạo phiếu
        addHistory("CREATE", "Tạo phiếu đặt buồng mới", createdBy);
    }

    // ── Tài chính ────────────────────────────────────────────────────────────

    public double calculateTotalPrice() {
        return pricePerNight * numberOfNights;
    }

    /**
     * Tính phí hủy:
     * - GUARANTEED: phạt 1 đêm nếu hủy trong vòng 24h trước check-in
     * - NON_GUARANTEED: không phạt (phòng chỉ giữ đến 18:00)
     */
    public double calculateCancellationFee() {
        if (reservationType == ReservationType.NON_GUARANTEED) return 0;

        long hoursUntilCheckIn = ChronoUnit.HOURS.between(
                LocalDateTime.now(),
                checkInDate.atTime(14, 0)   // Giờ check-in chuẩn 14:00
        );
        return hoursUntilCheckIn <= 24 ? pricePerNight : 0;
    }

    // ── Cập nhật trạng thái ──────────────────────────────────────────────────

    public void confirm(String employeeId) {
        this.status = ReservationStatus.CONFIRMED;
        addHistory("CONFIRM", "Xác nhận đặt buồng", employeeId);
    }

    public void cancel(String reason, String employeeId) {
        this.status       = ReservationStatus.CANCELLED;
        this.cancelReason = reason;
        addHistory("CANCEL", "Hủy đặt buồng - Lý do: " + reason, employeeId);
    }

    public void modify(String changeDetail, String employeeId) {
        this.status = ReservationStatus.MODIFIED;
        // Cập nhật lại số đêm nếu ngày đã đổi
        this.numberOfNights = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        addHistory("MODIFY", changeDetail, employeeId);
    }

    public void markNoShow(String employeeId) {
        this.status = ReservationStatus.NO_SHOW;
        addHistory("NO_SHOW", "Khách không đến", employeeId);
    }

    // ── Lịch sử ─────────────────────────────────────────────────────────────

    private void addHistory(String action, String description, String employeeId) {
        historyLog.add(new ReservationHistory(action, description, employeeId));
    }

    public List<ReservationHistory> getHistoryLog() {
        return new ArrayList<>(historyLog); // trả bản sao, bảo vệ dữ liệu gốc
    }

    // ── Inner Class: ReservationHistory ─────────────────────────────────────

    public static class ReservationHistory {
        private final String        action;       // CREATE / MODIFY / CANCEL ...
        private final String        description;
        private final String        employeeId;
        private final LocalDateTime timestamp;

        public ReservationHistory(String action, String description, String employeeId) {
            this.action      = action;
            this.description = description;
            this.employeeId  = employeeId;
            this.timestamp   = LocalDateTime.now();
        }

        public String        getAction()      { return action; }
        public String        getDescription() { return description; }
        public String        getEmployeeId()  { return employeeId; }
        public LocalDateTime getTimestamp()   { return timestamp; }

        @Override
        public String toString() {
            return String.format("[%s] %s - %s (by %s)",
                    timestamp, action, description, employeeId);
        }
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public String            getReservationId()              { return reservationId; }
    public Guest             getGuest()                      { return guest; }
    public String            getRegistrantName()             { return registrantName; }
    public void              setRegistrantName(String v)     { this.registrantName = v; }
    public Room              getRoom()                       { return room; }
    public void              setRoom(Room v)                 { this.room = v; }
    public int               getNumberOfGuests()             { return numberOfGuests; }
    public void              setNumberOfGuests(int v)        { this.numberOfGuests = v; }
    public LocalDate         getCheckInDate()                { return checkInDate; }
    public void              setCheckInDate(LocalDate v)     { this.checkInDate = v; }
    public LocalDate         getCheckOutDate()               { return checkOutDate; }
    public void              setCheckOutDate(LocalDate v)    { this.checkOutDate = v; }
    public int               getNumberOfNights()             { return numberOfNights; }
    public ReservationType   getReservationType()            { return reservationType; }
    public void              setReservationType(ReservationType v) { this.reservationType = v; }
    public ReservationStatus getStatus()                     { return status; }
    public BookingSource     getBookingSource()              { return bookingSource; }
    public double            getPricePerNight()              { return pricePerNight; }
    public void              setPricePerNight(double v)      { this.pricePerNight = v; }
    public String            getPaymentMethod()              { return paymentMethod; }
    public void              setPaymentMethod(String v)      { this.paymentMethod = v; }
    public String            getSpecialRequests()            { return specialRequests; }
    public void              setSpecialRequests(String v)    { this.specialRequests = v; }
    public String            getCancelReason()               { return cancelReason; }
    public LocalDateTime     getCreatedAt()                  { return createdAt; }
    public String            getCreatedBy()                  { return createdBy; }

    @Override
    public String toString() {
        return String.format(
            "Reservation{id='%s', guest='%s', room='%s', checkIn=%s, nights=%d, total=%.0f, status=%s}",
            reservationId, guest.getFullName(), room.getRoomId(),
            checkInDate, numberOfNights, calculateTotalPrice(), status);
    }
}
