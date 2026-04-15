package com.hotel.model;

import java.time.LocalDateTime;

public class Guest {

    private String guestId;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String idNumber;        // CMND / Hộ chiếu
    private String fax;
    private String registrantName;  // Người đăng ký (nếu khác khách)

    // ── Blacklist ────────────────────────────────────────────────────────────
    private boolean blacklisted;
    private String  blacklistReason;
    private LocalDateTime blacklistedAt;

    private LocalDateTime createdAt;

    public Guest(String guestId, String fullName, String phone,
                 String email, String address, String idNumber) {
        this.guestId   = guestId;
        this.fullName  = fullName;
        this.phone     = phone;
        this.email     = email;
        this.address   = address;
        this.idNumber  = idNumber;
        this.blacklisted = false;
        this.createdAt = LocalDateTime.now();
    }

    public Guest(String fullName, String phone, String email, String address) {
        this(null, fullName, phone, email, address, "");
    }

    // ── Blacklist operations ─────────────────────────────────────────────────

    public void addToBlacklist(String reason) {
        this.blacklisted      = true;
        this.blacklistReason  = reason;
        this.blacklistedAt    = LocalDateTime.now();
    }

    public void removeFromBlacklist() {
        this.blacklisted     = false;
        this.blacklistReason = null;
        this.blacklistedAt   = null;
    }

    public boolean isBlacklisted() { return blacklisted; }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public String getGuestId()              { return guestId; }
    public void   setGuestId(String v)      { this.guestId = v; }
    public String getFullName()             { return fullName; }
    public void   setFullName(String v)     { this.fullName = v; }
    public String getPhone()                { return phone; }
    public void   setPhone(String v)        { this.phone = v; }
    public String getEmail()                { return email; }
    public void   setEmail(String v)        { this.email = v; }
    public String getAddress()              { return address; }
    public void   setAddress(String v)      { this.address = v; }
    public String getIdNumber()             { return idNumber; }
    public void   setIdNumber(String v)     { this.idNumber = v; }
    public String getFax()                  { return fax; }
    public void   setFax(String v)          { this.fax = v; }
    public String getRegistrantName()       { return registrantName; }
    public void   setRegistrantName(String v) { this.registrantName = v; }
    public String getBlacklistReason()      { return blacklistReason; }
    public LocalDateTime getBlacklistedAt() { return blacklistedAt; }
    public LocalDateTime getCreatedAt()     { return createdAt; }

    @Override
    public String toString() {
        return String.format("Guest{id='%s', name='%s', blacklisted=%s}",
                guestId, fullName, blacklisted);
    }
}
