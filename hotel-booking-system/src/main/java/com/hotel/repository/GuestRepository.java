package com.hotel.repository;

import com.google.gson.reflect.TypeToken;
import com.hotel.model.Guest;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class GuestRepository extends JsonRepository<Guest, String> {

    private static final Type LIST_TYPE = new TypeToken<List<Guest>>(){}.getType();

    public GuestRepository() {
        super("guests.json", LIST_TYPE);
    }

    @Override
    protected String getId(Guest entity) {
        return entity.getGuestId();
    }

    public Optional<Guest> findByPhone(String phone) {
        return cache.stream()
                .filter(g -> g.getPhone().equals(phone))
                .findFirst();
    }

    public Optional<Guest> findByEmail(String email) {
        return cache.stream()
                .filter(g -> g.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public List<Guest> findBlacklisted() {
        return cache.stream().filter(Guest::isBlacklisted).toList();
    }
}
