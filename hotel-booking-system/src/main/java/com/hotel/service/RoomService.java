package com.hotel.service;

import com.hotel.exception.InvalidOperationException;
import com.hotel.model.Room;
import com.hotel.model.RoomStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class RoomService {

    private final List<Room> rooms;

    public RoomService() {
        this.rooms = new ArrayList<>();
    }

    public RoomService(List<Room> initialRooms) {
        this.rooms = new ArrayList<>(initialRooms);
    }

    public void addRoom(Room room) {
        if (room == null) {
            throw new InvalidOperationException("Thông tin phòng không được null");
        }
        rooms.add(room);
    }

    public Optional<Room> findById(String roomId) {
        return rooms.stream()
                .filter(r -> r.getRoomId().equals(roomId))
                .findFirst();
    }

    public List<Room> findAvailable() {
        return findByPredicate(Room::isAvailable);
    }

    public List<Room> findByType(String roomType) {
        return rooms.stream()
                .filter(r -> r.getRoomType().equalsIgnoreCase(roomType))
                .toList();
    }

    public List<Room> findByFloor(int floor) {
        return rooms.stream()
                .filter(r -> r.getFloor() == floor)
                .toList();
    }

    public List<Room> findByCapacity(int minCapacity) {
        return rooms.stream()
                .filter(r -> r.getCapacity() >= minCapacity)
                .toList();
    }

    public List<Room> findAvailableByTypeAndCapacity(String roomType, int minCapacity) {
        return rooms.stream()
                .filter(r -> r.getRoomType().equalsIgnoreCase(roomType))
                .filter(r -> r.getCapacity() >= minCapacity)
                .filter(Room::isAvailable)
                .toList();
    }

    public List<Room> findByPredicate(Predicate<Room> predicate) {
        return rooms.stream().filter(predicate).toList();
    }

    public List<Room> findByStatus(RoomStatus status) {
        return findByPredicate(r -> r.getStatus() == status);
    }

    public void updateRoomStatus(String roomId, RoomStatus newStatus) {
        Room room = findById(roomId)
                .orElseThrow(() -> new InvalidOperationException("Không tìm thấy phòng: " + roomId));
        room.setStatus(newStatus);
    }

    public void markAsOccupied(String roomId) {
        updateRoomStatus(roomId, RoomStatus.OCCUPIED);
    }

    public void markAsAvailable(String roomId) {
        updateRoomStatus(roomId, RoomStatus.AVAILABLE);
    }

    public void markAsCleaning(String roomId) {
        updateRoomStatus(roomId, RoomStatus.CLEANING);
    }

    public void markAsMaintenance(String roomId) {
        updateRoomStatus(roomId, RoomStatus.MAINTENANCE);
    }

    public void markAsOutOfOrder(String roomId) {
        updateRoomStatus(roomId, RoomStatus.OUT_OF_ORDER);
    }

    public Room updateRoom(String roomId, Room updated) {
        Room existing = findById(roomId)
                .orElseThrow(() -> new InvalidOperationException("Không tìm thấy phòng: " + roomId));
        
        if (updated.getRoomType() != null) existing.setRoomType(updated.getRoomType());
        if (updated.getPricePerNight() > 0) existing.setPricePerNight(updated.getPricePerNight());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        
        return existing;
    }

    public long countAvailable() {
        return rooms.stream().filter(Room::isAvailable).count();
    }

    public long countByStatus(RoomStatus status) {
        return rooms.stream().filter(r -> r.getStatus() == status).count();
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms);
    }

    public double calculateOccupancyRate() {
        if (rooms.isEmpty()) return 0;
        long occupied = countByStatus(RoomStatus.OCCUPIED);
        return (double) occupied / rooms.size() * 100;
    }
}
