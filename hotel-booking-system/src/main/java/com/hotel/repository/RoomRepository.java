package com.hotel.repository;

import com.hotel.model.Room;
import com.hotel.model.RoomStatus;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class RoomRepository extends JsonRepository<Room, String> {

    private static final Type LIST_TYPE = new TypeToken<List<Room>>(){}.getType();

    public RoomRepository() {
        super("rooms.json", LIST_TYPE);
    }

    @Override
    protected String getId(Room entity) {
        return entity.getRoomId();
    }

    public List<Room> findAvailable() {
        return cache.stream()
                .filter(Room::isAvailable)
                .toList();
    }

    public List<Room> findByType(String roomType) {
        return cache.stream()
                .filter(r -> r.getRoomType().equalsIgnoreCase(roomType))
                .toList();
    }

    public List<Room> findByStatus(RoomStatus status) {
        return cache.stream()
                .filter(r -> r.getStatus() == status)
                .toList();
    }

    public List<Room> findByFloor(int floor) {
        return cache.stream()
                .filter(r -> r.getFloor() == floor)
                .toList();
    }

    public List<Room> findByCapacity(int minCapacity) {
        return cache.stream()
                .filter(r -> r.getCapacity() >= minCapacity)
                .toList();
    }
}
