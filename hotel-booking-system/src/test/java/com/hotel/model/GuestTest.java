package com.hotel.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GuestTest {

    @Test
    void testNewGuestNotBlacklisted() {
        Guest guest = new Guest("G1", "Nguyen Van A", "0900000001",
                                "a@email.com", "Hanoi", "079000000001");
        assertFalse(guest.isBlacklisted());
    }

    @Test
    void testAddToBlacklist() {
        Guest guest = new Guest("G2", "Tran Thi B", "0900000002",
                                "b@email.com", "HCM", "079000000002");
        guest.addToBlacklist("Không thanh toán");
        assertTrue(guest.isBlacklisted());
        assertEquals("Không thanh toán", guest.getBlacklistReason());
        assertNotNull(guest.getBlacklistedAt());
    }

    @Test
    void testRemoveFromBlacklist() {
        Guest guest = new Guest("G3", "Le Van C", "0900000003",
                                "c@email.com", "DN", "079000000003");
        guest.addToBlacklist("Phá hoại tài sản");
        guest.removeFromBlacklist();
        assertFalse(guest.isBlacklisted());
        assertNull(guest.getBlacklistReason());
    }
}
