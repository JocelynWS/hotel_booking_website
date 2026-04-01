package com.hotel.model;

public class Room {

    private String     roomId;
    private String     roomType;       // Single, Double, Triple, Suite...
    private RoomStatus status;
    private double     pricePerNight;
    private int        floor;
    private int        capacity;       // Số khách tối đa
    private String     description;

    public Room(String roomId, String roomType, double pricePerNight,
                int floor, int capacity) {
        this.roomId        = roomId;
        this.roomType      = roomType;
        this.pricePerNight = pricePerNight;
        this.floor         = floor;
        this.capacity      = capacity;
        this.status        = RoomStatus.AVAILABLE;
    }

    // ── Logic ────────────────────────────────────────────────────────────────

    public boolean isAvailable() {
        return status == RoomStatus.AVAILABLE;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public String     getRoomId()              { return roomId; }
    public String     getRoomType()            { return roomType; }
    public void       setRoomType(String v)    { this.roomType = v; }
    public RoomStatus getStatus()              { return status; }
    public void       setStatus(RoomStatus v)  { this.status = v; }
    public double     getPricePerNight()       { return pricePerNight; }
    public void       setPricePerNight(double v){ this.pricePerNight = v; }
    public int        getFloor()               { return floor; }
    public int        getCapacity()            { return capacity; }
    public String     getDescription()         { return description; }
    public void       setDescription(String v) { this.description = v; }

    @Override
    public String toString() {
        return String.format("Room{id='%s', type='%s', price=%.0f, status=%s}",
                roomId, roomType, pricePerNight, status);
    }
}
