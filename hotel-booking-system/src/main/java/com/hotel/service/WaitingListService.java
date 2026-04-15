package com.hotel.service;

import com.hotel.exception.InvalidOperationException;
import com.hotel.model.Room;
import com.hotel.model.WaitingEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class WaitingListService {

    private final List<WaitingEntry> waitingList;

    public WaitingListService() {
        this.waitingList = new ArrayList<>();
    }

    public WaitingListService(List<WaitingEntry> initialList) {
        this.waitingList = new ArrayList<>(initialList);
    }

    public WaitingEntry addToWaitingList(WaitingEntry entry) {
        if (entry == null) {
            throw new InvalidOperationException("Thông tin danh sách chờ không được null");
        }
        waitingList.add(entry);
        return entry;
    }

    public Optional<WaitingEntry> findById(String waitingId) {
        return waitingList.stream()
                .filter(w -> w.getWaitingId().equals(waitingId))
                .findFirst();
    }

    public List<WaitingEntry> findByGuestId(String guestId) {
        return waitingList.stream()
                .filter(w -> w.getGuest().getGuestId().equals(guestId))
                .toList();
    }

    public List<WaitingEntry> findByRoomType(String roomType) {
        return waitingList.stream()
                .filter(w -> w.getRoomType().equalsIgnoreCase(roomType))
                .toList();
    }

    public List<WaitingEntry> findNotNotified() {
        return waitingList.stream()
                .filter(w -> !w.isNotified())
                .toList();
    }

    public List<WaitingEntry> findByPredicate(Predicate<WaitingEntry> predicate) {
        return waitingList.stream().filter(predicate).toList();
    }

    public List<WaitingEntry> findMatchingEntries(Room room) {
        return waitingList.stream()
                .filter(WaitingEntry::matchesRoom)
                .filter(w -> !w.isNotified())
                .sorted(Comparator.comparing(WaitingEntry::getRegisteredAt))
                .toList();
    }

    public List<WaitingEntry> findMatchingEntriesByRoomType(Room room, String roomType) {
        return waitingList.stream()
                .filter(w -> w.getRoomType().equalsIgnoreCase(roomType))
                .filter(w -> w.getRoom().getCapacity() >= room.getCapacity())
                .filter(w -> !w.isNotified())
                .sorted(Comparator.comparing(WaitingEntry::getRegisteredAt))
                .toList();
    }

    public void markAsNotified(String waitingId) {
        WaitingEntry entry = findById(waitingId)
                .orElseThrow(() -> new InvalidOperationException("Không tìm thấy mục chờ: " + waitingId));
        entry.markNotified();
    }

    public void removeFromWaitingList(String waitingId) {
        WaitingEntry entry = findById(waitingId)
                .orElseThrow(() -> new InvalidOperationException("Không tìm thấy mục chờ: " + waitingId));
        waitingList.remove(entry);
    }

    public void removeByGuestId(String guestId) {
        waitingList.removeIf(w -> w.getGuest().getGuestId().equals(guestId));
    }

    public void removeExpiredEntries(java.time.LocalDate beforeDate) {
        waitingList.removeIf(w -> w.getDesiredCheckIn().isBefore(beforeDate));
    }

    public int countWaiting() {
        return waitingList.size();
    }

    public int countWaitingByRoomType(String roomType) {
        return (int) findByRoomType(roomType).size();
    }

    public List<WaitingEntry> getAllWaitingEntries() {
        return new ArrayList<>(waitingList);
    }

    public List<WaitingEntry> getWaitingEntriesSortedByRegistration() {
        return waitingList.stream()
                .sorted(Comparator.comparing(WaitingEntry::getRegisteredAt))
                .toList();
    }
}
