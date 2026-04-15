package com.hotel.repository;

import com.hotel.model.WaitingEntry;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;

public class WaitingEntryRepository extends JsonRepository<WaitingEntry, String> {

    private static final Type LIST_TYPE = new TypeToken<List<WaitingEntry>>(){}.getType();

    public WaitingEntryRepository() {
        super("waiting_list.json", LIST_TYPE);
    }

    @Override
    protected String getId(WaitingEntry entity) {
        return entity.getWaitingId();
    }

    public List<WaitingEntry> findByGuestId(String guestId) {
        return cache.stream()
                .filter(w -> w.getGuest().getGuestId().equals(guestId))
                .toList();
    }

    public List<WaitingEntry> findByRoomType(String roomType) {
        return cache.stream()
                .filter(w -> w.getRoomType().equalsIgnoreCase(roomType))
                .toList();
    }

    public List<WaitingEntry> findNotNotified() {
        return cache.stream()
                .filter(w -> !w.isNotified())
                .toList();
    }

    public List<WaitingEntry> findExpired(LocalDate beforeDate) {
        return cache.stream()
                .filter(w -> w.getDesiredCheckIn().isBefore(beforeDate))
                .toList();
    }

    public void removeExpiredEntries(LocalDate beforeDate) {
        cache.removeIf(w -> w.getDesiredCheckIn().isBefore(beforeDate));
        saveToFile();
    }
}
