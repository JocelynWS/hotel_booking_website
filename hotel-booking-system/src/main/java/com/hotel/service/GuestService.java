package com.hotel.service;

import com.hotel.exception.GuestBlacklistedException;
import com.hotel.exception.InvalidOperationException;
import com.hotel.model.Guest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class GuestService {

    private final List<Guest> guests;

    public GuestService() {
        this.guests = new ArrayList<>();
    }

    public GuestService(List<Guest> initialGuests) {
        this.guests = new ArrayList<>(initialGuests);
    }

    public Guest registerGuest(Guest guest) {
        if (guest == null) {
            throw new InvalidOperationException("Thông tin khách hàng không được null");
        }
        guests.add(guest);
        return guest;
    }

    public Optional<Guest> findById(String guestId) {
        return guests.stream()
                .filter(g -> g.getGuestId().equals(guestId))
                .findFirst();
    }

    public Optional<Guest> findByPhone(String phone) {
        return guests.stream()
                .filter(g -> g.getPhone().equals(phone))
                .findFirst();
    }

    public Optional<Guest> findByEmail(String email) {
        return guests.stream()
                .filter(g -> g.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public List<Guest> findByPredicate(Predicate<Guest> predicate) {
        return guests.stream().filter(predicate).toList();
    }

    public List<Guest> findBlacklisted() {
        return findByPredicate(Guest::isBlacklisted);
    }

    public void validateGuest(Guest guest) {
        if (guest.isBlacklisted()) {
            throw new GuestBlacklistedException(guest.getGuestId(), guest.getBlacklistReason());
        }
    }

    public void addToBlacklist(String guestId, String reason, String authorizedBy) {
        Guest guest = findById(guestId)
                .orElseThrow(() -> new InvalidOperationException("Không tìm thấy khách: " + guestId));
        guest.addToBlacklist(reason);
    }

    public void removeFromBlacklist(String guestId) {
        Guest guest = findById(guestId)
                .orElseThrow(() -> new InvalidOperationException("Không tìm thấy khách: " + guestId));
        guest.removeFromBlacklist();
    }

    public Guest updateGuest(String guestId, Guest updated) {
        Guest existing = findById(guestId)
                .orElseThrow(() -> new InvalidOperationException("Không tìm thấy khách: " + guestId));
        
        if (updated.getFullName() != null) existing.setFullName(updated.getFullName());
        if (updated.getPhone() != null) existing.setPhone(updated.getPhone());
        if (updated.getEmail() != null) existing.setEmail(updated.getEmail());
        if (updated.getAddress() != null) existing.setAddress(updated.getAddress());
        if (updated.getIdNumber() != null) existing.setIdNumber(updated.getIdNumber());
        
        return existing;
    }

    public List<Guest> getAllGuests() {
        return new ArrayList<>(guests);
    }
}
