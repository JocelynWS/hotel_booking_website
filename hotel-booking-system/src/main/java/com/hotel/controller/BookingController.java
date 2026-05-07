package com.hotel.controller;

import com.hotel.model.*;
import com.hotel.repository.HotelRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
public class BookingController {

    private final HotelRepository repo = new HotelRepository();

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("rooms", repo.getAllRooms());
        model.addAttribute("totalRooms", repo.countTotalRooms());
        model.addAttribute("availableRooms", repo.countAvailableRooms());
        model.addAttribute("occupiedRooms", repo.countOccupiedRooms());
        model.addAttribute("occupancyRate", repo.getOccupancyRate());

        List<Map<String, Object>> roomTypeSummaries = new ArrayList<>();
        List<Map<String, Object>> roomTypesList = new ArrayList<>();

        List<String> allTypes = List.of("Single", "Double", "Triple", "Suite", "VIP Suite");
        Map<String, String> typeDescriptions = Map.of(
            "Single", "Phòng đơn tiện nghi, thích hợp cho khách đi công tác hoặc du lịch một mình",
            "Double", "Phòng đôi rộng rãi với giường lớn, phù hợp cho cặp đôi hoặc gia đình nhỏ",
            "Triple", "Phòng ba thoải mái, trang bị đầy đủ tiện nghi cho gia đình hoặc nhóm bạn",
            "Suite", "Phòng suite sang trọng với khu vực tiếp khách riêng, tầm nhìn đẹp",
            "VIP Suite", "Phòng VIP cao cấp nhất, không gian rộng lớn, tiện ích đẳng cấp 5 sao"
        );
        Map<String, String> typeIcons = Map.of(
            "Single", "fa-user",
            "Double", "fa-user-group",
            "Triple", "fa-people-group",
            "Suite", "fa-crown",
            "VIP Suite", "fa-gem"
        );
        Map<String, List<String>> typeAmenities = Map.of(
            "Single", List.of("Wifi miễn phí", "Điều hòa", "TV màn hình phẳng", "Phòng tắm riêng"),
            "Double", List.of("Wifi miễn phí", "Điều hòa", "TV 43 inch", "Mini bar", "Phòng tắm riêng"),
            "Triple", List.of("Wifi miễn phí", "Điều hòa", "TV 43 inch", "Mini bar", "Ban công", "Phòng tắm riêng"),
            "Suite", List.of("Wifi miễn phí", "Điều hòa", "TV 55 inch", "Mini bar", "Ban công", "Phòng khách riêng", "Bồn tắm"),
            "VIP Suite", List.of("Wifi miễn phí", "Điều hòa", "TV 65 inch", "Mini bar cao cấp", "Ban công panorama", "Phòng khách rộng", "Bồn tắm Jacuzzi", "Phòng xông hơi")
        );

        for (String type : allTypes) {
            List<Room> typeRooms = repo.findRoomsByType(type);
            if (typeRooms.isEmpty()) continue;

            double minPrice = typeRooms.stream().mapToDouble(Room::getPricePerNight).min().orElse(0);
            double maxPrice = typeRooms.stream().mapToDouble(Room::getPricePerNight).max().orElse(0);
            long availableCount = typeRooms.stream().filter(Room::isAvailable).count();
            int maxCapacity = typeRooms.stream().mapToInt(Room::getCapacity).max().orElse(0);

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("roomType", type);
            summary.put("description", typeDescriptions.getOrDefault(type, ""));
            summary.put("icon", typeIcons.getOrDefault(type, "fa-bed"));
            summary.put("amenities", typeAmenities.getOrDefault(type, List.of()));
            summary.put("minPrice", minPrice);
            summary.put("maxPrice", maxPrice);
            summary.put("priceDisplay", minPrice == maxPrice
                ? formatPrice(minPrice)
                : formatPrice(minPrice) + " - " + formatPrice(maxPrice));
            summary.put("availableCount", availableCount);
            summary.put("totalCount", typeRooms.size());
            summary.put("maxCapacity", maxCapacity);

            roomTypeSummaries.add(summary);

            Map<String, Object> typeOption = new LinkedHashMap<>();
            typeOption.put("type", type);
            typeOption.put("price", formatPrice(minPrice));
            roomTypesList.add(typeOption);
        }

        model.addAttribute("roomTypeSummaries", roomTypeSummaries);
        model.addAttribute("roomTypesForSelect", roomTypesList);
        return "index";
    }

    private String formatPrice(double price) {
        if (price >= 1_000_000) {
            return String.format("%.1f tr", price / 1_000_000);
        }
        return String.format("%,.0fđ", price);
    }

    @GetMapping("/step1")
    public String step1(Model model) {
        model.addAttribute("roomTypes", List.of("Single", "Double", "Triple", "Suite", "VIP Suite"));
        model.addAttribute("paymentMethods", List.of("Tiền mặt", "Chuyển khoản", "Thẻ tín dụng", "Ví điện tử"));
        model.addAttribute("bookingSources", List.of("Gặp trực tiếp", "Điện thoại", "Fax", "Email", "Internet"));
        return "step1";
    }

    @PostMapping("/step1/submit")
    public String submitStep1(
            @RequestParam String guestName,
            @RequestParam(required = false) String registrantName,
            @RequestParam(required = false) String address,
            @RequestParam String phone,
            @RequestParam(required = false) String fax,
            @RequestParam(required = false) String email,
            @RequestParam int numberOfGuests,
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam int numberOfRooms,
            @RequestParam String roomType,
            @RequestParam String paymentMethod,
            @RequestParam String reservationType,
            @RequestParam String bookingSource,
            @RequestParam(required = false) String specialRequests,
            Model model) {

        LocalDate checkInDate = LocalDate.parse(checkIn);
        LocalDate checkOutDate = LocalDate.parse(checkOut);
        int nights = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);

        List<Room> availableRooms = repo.findAvailableRoomsForPeriod(roomType, numberOfGuests, checkInDate, checkOutDate);

        if (availableRooms.isEmpty()) {
            model.addAttribute("error", "Không có phòng trống cho yêu cầu này!");
            model.addAttribute("roomTypes", List.of("Single", "Double", "Triple", "Suite", "VIP Suite"));
            return "step1";
        }

        model.addAttribute("guestName", guestName);
        model.addAttribute("registrantName", registrantName != null ? registrantName : guestName);
        model.addAttribute("address", address);
        model.addAttribute("phone", phone);
        model.addAttribute("fax", fax);
        model.addAttribute("email", email);
        model.addAttribute("numberOfGuests", numberOfGuests);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("nights", nights);
        model.addAttribute("numberOfRooms", numberOfRooms);
        model.addAttribute("roomType", roomType);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("reservationType", reservationType);
        model.addAttribute("bookingSource", bookingSource);
        model.addAttribute("specialRequests", specialRequests);
        model.addAttribute("availableRooms", availableRooms);

        Room selectedRoom = availableRooms.get(0);
        model.addAttribute("selectedRoom", selectedRoom);
        model.addAttribute("totalPrice", selectedRoom.getPricePerNight() * nights);

        return "step1_confirm";
    }

    @PostMapping("/step1/book")
    public String bookRoom(
            @RequestParam String guestName,
            @RequestParam String registrantName,
            @RequestParam String address,
            @RequestParam String phone,
            @RequestParam String fax,
            @RequestParam String email,
            @RequestParam int numberOfGuests,
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam String roomId,
            @RequestParam String roomType,
            @RequestParam String paymentMethod,
            @RequestParam String reservationType,
            @RequestParam String bookingSource,
            @RequestParam(required = false) String specialRequests,
            Model model) {

        LocalDate checkInDate = LocalDate.parse(checkIn);
        LocalDate checkOutDate = LocalDate.parse(checkOut);
        int nights = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);

        Guest guest = new Guest(guestName, phone, email, address);
        guest.setRegistrantName(registrantName);
        guest.setFax(fax);

        Room room = repo.findRoomById(roomId).orElseThrow();
        ReservationType resType = reservationType.equals("Đảm bảo") ? ReservationType.GUARANTEED : ReservationType.NON_GUARANTEED;
        BookingSource source = BookingSource.valueOf(bookingSource.toUpperCase().replace(" ", "_"));

        double totalPrice = room.getPricePerNight() * nights;

        if (paymentMethod.equals("Chuyển khoản") || paymentMethod.equals("Thẻ tín dụng") || paymentMethod.equals("Ví điện tử")) {
            model.addAttribute("guestName", guestName);
            model.addAttribute("registrantName", registrantName);
            model.addAttribute("address", address);
            model.addAttribute("phone", phone);
            model.addAttribute("fax", fax);
            model.addAttribute("email", email);
            model.addAttribute("numberOfGuests", numberOfGuests);
            model.addAttribute("checkIn", checkIn);
            model.addAttribute("checkOut", checkOut);
            model.addAttribute("roomId", roomId);
            model.addAttribute("roomType", roomType);
            model.addAttribute("paymentMethod", paymentMethod);
            model.addAttribute("reservationType", reservationType);
            model.addAttribute("bookingSource", bookingSource);
            model.addAttribute("specialRequests", specialRequests != null ? specialRequests : "");
            model.addAttribute("room", room);
            model.addAttribute("nights", nights);
            model.addAttribute("totalPrice", totalPrice);

            return "payment";
        }

        repo.saveGuest(guest);

        Reservation reservation = repo.createReservation(
                guest, room, checkInDate, checkOutDate,
                numberOfGuests, resType, source,
                paymentMethod, specialRequests != null ? specialRequests : ""
        );

        repo.updateRoomStatus(roomId, RoomStatus.OCCUPIED);

        model.addAttribute("reservation", reservation);
        model.addAttribute("guest", guest);
        model.addAttribute("room", room);
        model.addAttribute("nights", nights);
        model.addAttribute("totalPrice", totalPrice);

        return "success";
    }

    @PostMapping("/payment/confirm")
    public String confirmPayment(
            @RequestParam String guestName,
            @RequestParam String registrantName,
            @RequestParam String address,
            @RequestParam String phone,
            @RequestParam String fax,
            @RequestParam String email,
            @RequestParam int numberOfGuests,
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam String roomId,
            @RequestParam String roomType,
            @RequestParam String paymentMethod,
            @RequestParam String reservationType,
            @RequestParam String bookingSource,
            @RequestParam String specialRequests,
            @RequestParam String transactionCode,
            Model model) {

        LocalDate checkInDate = LocalDate.parse(checkIn);
        LocalDate checkOutDate = LocalDate.parse(checkOut);
        int nights = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);

        Guest guest = new Guest(guestName, phone, email, address);
        guest.setRegistrantName(registrantName);
        guest.setFax(fax);
        repo.saveGuest(guest);

        Room room = repo.findRoomById(roomId).orElseThrow();
        ReservationType resType = reservationType.equals("Đảm bảo") ? ReservationType.GUARANTEED : ReservationType.NON_GUARANTEED;
        BookingSource source = BookingSource.valueOf(bookingSource.toUpperCase().replace(" ", "_"));

        String paymentDetail = paymentMethod + " (Mã GD: " + transactionCode + ")";

        Reservation reservation = repo.createReservation(
                guest, room, checkInDate, checkOutDate,
                numberOfGuests, resType, source,
                paymentDetail, specialRequests
        );

        repo.updateRoomStatus(roomId, RoomStatus.OCCUPIED);

        model.addAttribute("reservation", reservation);
        model.addAttribute("guest", guest);
        model.addAttribute("room", room);
        model.addAttribute("nights", nights);
        model.addAttribute("totalPrice", room.getPricePerNight() * nights);
        model.addAttribute("transactionCode", transactionCode);

        return "success";
    }

    @GetMapping("/step2")
    public String step2(Model model) {
        model.addAttribute("roomTypes", List.of("Single", "Double", "Triple", "Suite", "VIP Suite"));
        return "step2";
    }

    @PostMapping("/step2/check")
    public String checkAvailability(
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam String roomType,
            @RequestParam int guests,
            Model model) {

        LocalDate checkInDate = LocalDate.parse(checkIn);
        LocalDate checkOutDate = LocalDate.parse(checkOut);
        int nights = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);

        HotelRepository.RoomAvailability availability = repo.analyzeAvailability(checkInDate, checkOutDate);
        List<Room> availableRooms = repo.findAvailableRoomsForPeriod(roomType, guests, checkInDate, checkOutDate);

        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("roomType", roomType);
        model.addAttribute("guests", guests);
        model.addAttribute("nights", nights);
        model.addAttribute("availability", availability);
        model.addAttribute("availableRooms", availableRooms);

        List<String> allTypes = List.of("Single", "Double", "Triple", "Suite", "VIP Suite");
        int currentIndex = allTypes.indexOf(roomType);
        
        List<Map<String, Object>> upgrades = new ArrayList<>();
        for (int i = 1; i <= 2 && currentIndex + i < allTypes.size(); i++) {
            String upgradeType = allTypes.get(currentIndex + i);
            List<Room> upgradeRooms = repo.findAvailableRoomsForPeriod(upgradeType, guests, checkInDate, checkOutDate);
            if (!upgradeRooms.isEmpty()) {
                Map<String, Object> upgrade = new HashMap<>();
                upgrade.put("type", upgradeType);
                upgrade.put("rooms", upgradeRooms);
                upgrades.add(upgrade);
            }
        }
        model.addAttribute("upgrades", upgrades);

        return "step2_result";
    }

    @GetMapping("/rooms")
    @ResponseBody
    public List<Room> getAllRooms() {
        return repo.getAllRooms();
    }

    @GetMapping("/api/rooms")
    @ResponseBody
    public List<Map<String, Object>> getAvailableRooms(
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) Integer guests) {
        
        LocalDate checkInDate = LocalDate.parse(checkIn);
        LocalDate checkOutDate = LocalDate.parse(checkOut);
        
        List<Room> rooms;
        if (roomType != null && guests != null) {
            rooms = repo.findAvailableRoomsForPeriod(roomType, guests, checkInDate, checkOutDate);
        } else {
            rooms = repo.findAvailableRooms();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Room r : rooms) {
            Map<String, Object> map = new HashMap<>();
            map.put("roomId", r.getRoomId());
            map.put("roomType", r.getRoomType());
            map.put("floor", r.getFloor());
            map.put("capacity", r.getCapacity());
            map.put("pricePerNight", r.getPricePerNight());
            map.put("status", r.getStatus());
            map.put("available", r.isAvailable());
            result.add(map);
        }
        return result;
    }
}
