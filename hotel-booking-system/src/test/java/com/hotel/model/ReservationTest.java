package com.hotel.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ReservationTest {

    private Guest guest;
    private Room  room;

    @BeforeEach
    void setUp() {
        guest = new Guest("G1", "Test Guest", "0900000001",
                          "test@email.com", "Hanoi", "079000000001");
        room  = new Room("R01", "Double", 500_000, 1, 2);
    }

    @Test
    void testNumberOfNightsCalculated() {
        LocalDate in  = LocalDate.of(2025, 8, 1);
        LocalDate out = LocalDate.of(2025, 8, 4);
        Reservation res = makeReservation(in, out, ReservationType.GUARANTEED);
        assertEquals(3, res.getNumberOfNights());
    }

    @Test
    void testTotalPriceCalculation() {
        LocalDate in  = LocalDate.of(2025, 8, 1);
        LocalDate out = LocalDate.of(2025, 8, 3);
        Reservation res = makeReservation(in, out, ReservationType.GUARANTEED);
        assertEquals(1_000_000, res.calculateTotalPrice(), 0.01);
    }

    @Test
    void testNonGuaranteedNoCancellationFee() {
        LocalDate in  = LocalDate.now().plusDays(1);
        LocalDate out = LocalDate.now().plusDays(3);
        Reservation res = makeReservation(in, out, ReservationType.NON_GUARANTEED);
        assertEquals(0, res.calculateCancellationFee(), 0.01);
    }

    @Test
    void testCancelAddsHistory() {
        LocalDate in  = LocalDate.now().plusDays(5);
        LocalDate out = LocalDate.now().plusDays(7);
        Reservation res = makeReservation(in, out, ReservationType.GUARANTEED);
        res.cancel("Đổi kế hoạch", "EMP001");

        assertEquals(ReservationStatus.CANCELLED, res.getStatus());
        assertTrue(res.getHistoryLog().stream()
                      .anyMatch(h -> h.getAction().equals("CANCEL")));
    }

    @Test
    void testHistoryLogHasCreateEntry() {
        LocalDate in  = LocalDate.now().plusDays(5);
        LocalDate out = LocalDate.now().plusDays(6);
        Reservation res = makeReservation(in, out, ReservationType.GUARANTEED);
        assertEquals(1, res.getHistoryLog().size());
        assertEquals("CREATE", res.getHistoryLog().get(0).getAction());
    }

    private Reservation makeReservation(LocalDate in, LocalDate out,
                                        ReservationType type) {
        return new Reservation(
            "RES-TEST-001", guest, room, in, out,
            2, type, BookingSource.DIRECT, "Cash", "EMP001"
        );
    }
}
