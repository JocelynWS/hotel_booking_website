package com.hotel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WaitingEntry {

    private String        waitingId;
    private Guest         guest;
    private String        roomType;          // Loại phòng mong muốn
    private LocalDate     desiredCheckIn;
    private LocalDate     desiredCheckOut;
    private int           numberOfGuests;
    private String        contactNote;       // Ghi chú liên lạc
    private boolean       notified;          // Đã thông báo phòng trống chưa
    private LocalDateTime registeredAt;

    public WaitingEntry(String waitingId, Guest guest, String roomType,
                        LocalDate desiredCheckIn, LocalDate desiredCheckOut,
                        int numberOfGuests, String contactNote) {
        this.waitingId       = waitingId;
        this.guest           = guest;
        this.roomType        = roomType;
        this.desiredCheckIn  = desiredCheckIn;
        this.desiredCheckOut = desiredCheckOut;
        this.numberOfGuests  = numberOfGuests;
        this.contactNote     = contactNote;
        this.notified        = false;
        this.registeredAt    = LocalDateTime.now();
    }

    // ── Logic ────────────────────────────────────────────────────────────────

    public void markNotified() {
        this.notified = true;
    }

    /**
     * Kiểm tra xem một phòng vừa trống có phù hợp với nhu cầu của khách chờ không.
     */
    public boolean matchesRoom(Room room) {
        return room.getRoomType().equalsIgnoreCase(this.roomType)
            && room.isAvailable()
            && room.getCapacity() >= this.numberOfGuests;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public String        getWaitingId()               { return waitingId; }
    public Guest         getGuest()                   { return guest; }
    public String        getRoomType()                { return roomType; }
    public LocalDate     getDesiredCheckIn()          { return desiredCheckIn; }
    public LocalDate     getDesiredCheckOut()         { return desiredCheckOut; }
    public int           getNumberOfGuests()          { return numberOfGuests; }
    public String        getContactNote()             { return contactNote; }
    public void          setContactNote(String v)     { this.contactNote = v; }
    public boolean       isNotified()                 { return notified; }
    public LocalDateTime getRegisteredAt()            { return registeredAt; }

    @Override
    public String toString() {
        return String.format(
            "WaitingEntry{id='%s', guest='%s', roomType='%s', checkIn=%s, notified=%s}",
            waitingId, guest.getFullName(), roomType, desiredCheckIn, notified);
    }
}
