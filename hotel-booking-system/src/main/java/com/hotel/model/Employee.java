package com.hotel.model;

import java.time.LocalDateTime;

public class Employee {

    private String employeeId;
    private String fullName;
    private String phone;
    private String email;
    private EmployeeRole role;
    private String passwordHash;
    private LocalDateTime createdAt;
    private boolean active;

    public Employee(String employeeId, String fullName, String phone,
                    String email, EmployeeRole role, String passwordHash) {
        this.employeeId  = employeeId;
        this.fullName    = fullName;
        this.phone       = phone;
        this.email       = email;
        this.role        = role;
        this.passwordHash = passwordHash;
        this.createdAt   = LocalDateTime.now();
        this.active      = true;
    }

    // ── Phân quyền ──────────────────────────────────────────────────────────

    public boolean canCreateReservation() {
        return role == EmployeeRole.RESERVATION_STAFF
            || role == EmployeeRole.MANAGER;
    }

    public boolean canCancelReservation() {
        return role == EmployeeRole.RESERVATION_STAFF
            || role == EmployeeRole.MANAGER;
    }

    public boolean canModifyReservation() {
        return role == EmployeeRole.RESERVATION_STAFF
            || role == EmployeeRole.MANAGER;
    }

    public boolean canManageBlacklist() {
        return role == EmployeeRole.MANAGER;
    }

    public boolean canViewReports() {
        return role == EmployeeRole.MANAGER;
    }

    public boolean canManageRooms() {
        return role == EmployeeRole.MANAGER;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public String getEmployeeId()           { return employeeId; }
    public String getFullName()             { return fullName; }
    public void   setFullName(String v)     { this.fullName = v; }
    public String getPhone()                { return phone; }
    public void   setPhone(String v)        { this.phone = v; }
    public String getEmail()                { return email; }
    public void   setEmail(String v)        { this.email = v; }
    public EmployeeRole getRole()           { return role; }
    public void   setRole(EmployeeRole v)   { this.role = v; }
    public String getPasswordHash()         { return passwordHash; }
    public void   setPasswordHash(String v) { this.passwordHash = v; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public boolean isActive()               { return active; }
    public void   setActive(boolean v)      { this.active = v; }

    @Override
    public String toString() {
        return String.format("Employee{id='%s', name='%s', role=%s, active=%s}",
                employeeId, fullName, role, active);
    }
}
