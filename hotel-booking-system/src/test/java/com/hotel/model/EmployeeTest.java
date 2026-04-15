package com.hotel.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    @Test
    void testStaffPermissions() {
        Employee staff = new Employee("E1", "Staff A", "090", "a@h.com",
                                      EmployeeRole.RESERVATION_STAFF, "pw");
        assertTrue(staff.canCreateReservation());
        assertTrue(staff.canCancelReservation());
        assertFalse(staff.canManageBlacklist());
        assertFalse(staff.canViewReports());
    }

    @Test
    void testManagerPermissions() {
        Employee manager = new Employee("E2", "Manager B", "091", "b@h.com",
                                        EmployeeRole.MANAGER, "pw");
        assertTrue(manager.canCreateReservation());
        assertTrue(manager.canManageBlacklist());
        assertTrue(manager.canViewReports());
        assertTrue(manager.canManageRooms());
    }

    @Test
    void testReceptionistPermissions() {
        Employee receptionist = new Employee("E3", "Recept C", "092", "c@h.com",
                                             EmployeeRole.RECEPTIONIST, "pw");
        assertFalse(receptionist.canCreateReservation());
        assertFalse(receptionist.canManageBlacklist());
    }
}
