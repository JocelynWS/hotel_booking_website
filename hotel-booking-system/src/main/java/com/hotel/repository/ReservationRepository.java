package com.hotel.repository;

import com.hotel.model.Reservation;
import com.hotel.model.ReservationStatus;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;

public class ReservationRepository extends JsonRepository<Reservation, String> {

    private static final Type LIST_TYPE = new TypeToken<List<Reservation>>(){}.getType();

    public ReservationRepository() {
        super("reservations.json", LIST_TYPE);
    }

    @Override
    protected String getId(Reservation entity) {
        return entity.getReservationId();
    }

    public List<Reservation> findByGuestId(String guestId) {
        return cache.stream()
                .filter(r -> r.getGuest().getGuestId().equals(guestId))
                .toList();
    }

    public List<Reservation> findByStatus(ReservationStatus status) {
        return cache.stream()
                .filter(r -> r.getStatus() == status)
                .toList();
    }

    public List<Reservation> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return cache.stream()
                .filter(r -> !(r.getCheckOutDate().isBefore(startDate) || 
                              r.getCheckInDate().isAfter(endDate)))
                .toList();
    }

    public List<Reservation> findByCheckInDate(LocalDate date) {
        return cache.stream()
                .filter(r -> r.getCheckInDate().equals(date))
                .toList();
    }

    public List<Reservation> findByCheckOutDate(LocalDate date) {
        return cache.stream()
                .filter(r -> r.getCheckOutDate().equals(date))
                .toList();
    }

    public List<Reservation> findActiveReservations(String roomId, LocalDate checkIn, LocalDate checkOut) {
        return cache.stream()
                .filter(r -> r.getRoom().getRoomId().equals(roomId))
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .filter(r -> r.getStatus() != ReservationStatus.NO_SHOW)
                .filter(r -> !(checkOut.isBefore(r.getCheckInDate()) || 
                              checkIn.isAfter(r.getCheckOutDate().minusDays(1))))
                .toList();
    }
}
