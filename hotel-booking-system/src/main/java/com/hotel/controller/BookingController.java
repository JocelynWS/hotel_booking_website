package com.hotel.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hotel.model.BookingSource;
import com.hotel.model.Guest;
import com.hotel.model.Reservation;
import com.hotel.model.ReservationStatus;
import com.hotel.model.ReservationType;
import com.hotel.model.Room;
import com.hotel.model.RoomStatus;
import com.hotel.repository.HotelRepository;
import com.hotel.service.BusinessLogicService;
import com.hotel.service.DatabaseManager;

import jakarta.servlet.http.HttpSession;

@Controller
public class BookingController {

    private final HotelRepository repo;
    private final DatabaseManager dbManager;
    private final BusinessLogicService logicService;

    @Autowired
    public BookingController(HotelRepository repo, DatabaseManager dbManager, BusinessLogicService logicService) {
        this.repo = repo;
        this.dbManager = dbManager;
        this.logicService = logicService;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(required = false) String checkIn,
            @RequestParam(required = false) String checkOut,
            Model model) {
            
        LocalDate checkInDate = checkIn != null && !checkIn.isEmpty() ? LocalDate.parse(checkIn) : null;
        LocalDate checkOutDate = checkOut != null && !checkOut.isEmpty() ? LocalDate.parse(checkOut) : null;
        boolean isSearched = (checkInDate != null && checkOutDate != null);

        model.addAttribute("isSearched", isSearched);
        model.addAttribute("searchCheckIn", checkIn);
        model.addAttribute("searchCheckOut", checkOut);

        model.addAttribute("rooms", repo.getAllRooms());
        model.addAttribute("totalRooms", repo.countTotalRooms());
        model.addAttribute("availableRooms", repo.countAvailableRooms());
        model.addAttribute("occupiedRooms", repo.countOccupiedRooms());
        model.addAttribute("occupancyRate", repo.getOccupancyRate());

        List<Map<String, Object>> roomTypeSummaries = new ArrayList<>();
        List<Map<String, Object>> roomTypesList = new ArrayList<>();

        List<String> allTypes = List.of("Deluxe City View", "Deluxe Triple City View", "Executive Suite City View", "Executive Twin City View", "Presidential Suite");
        Map<String, String> typeDescriptions = Map.of(
            "Deluxe City View", "Phòng Deluxe hướng thành phố với tầm nhìn đẹp, tiện nghi hiện đại, bao gồm ăn sáng",
            "Deluxe Triple City View", "Phòng Deluxe cho 3 khách hướng thành phố, rộng rãi và thoải mái, bao gồm ăn sáng",
            "Executive Suite City View", "Suite Executive hướng thành phố sang trọng, không gian riêng tư, bao gồm ăn sáng",
            "Executive Twin City View", "Suite Executive cho 3 khách hướng thành phố, thiết kế tinh tế, bao gồm ăn sáng",
            "Presidential Suite", "Phòng Presidential Suite cao cấp nhất, không gian rộng lớn, tiện ích đẳng cấp, bao gồm ăn sáng"
        );
        Map<String, String> typeIcons = Map.of(
            "Deluxe City View", "fa-city",
            "Deluxe Triple City View", "fa-users",
            "Executive Suite City View", "fa-star",
            "Executive Twin City View", "fa-users-gear",
            "Presidential Suite", "fa-crown"
        );
        Map<String, List<String>> typeAmenities = Map.of(
            "Deluxe City View", List.of("Wifi miễn phí", "Điều hòa", "TV màn hình phẳng", "Ăn sáng miễn phí", "Phòng tắm riêng"),
            "Deluxe Triple City View", List.of("Wifi miễn phí", "Điều hòa", "TV 43 inch", "Ăn sáng miễn phí", "Mini bar", "Phòng tắm riêng"),
            "Executive Suite City View", List.of("Wifi miễn phí", "Điều hòa", "TV 55 inch", "Ăn sáng miễn phí", "Mini bar", "Ban công", "Phòng khách riêng"),
            "Executive Twin City View", List.of("Wifi miễn phí", "Điều hòa", "TV 55 inch", "Ăn sáng miễn phí", "Mini bar", "Ban công", "Phòng khách riêng"),
            "Presidential Suite", List.of("Wifi miễn phí", "Điều hòa", "TV 65 inch", "Ăn sáng miễn phí", "Mini bar cao cấp", "Ban công panorama", "Phòng khách rộng", "Bồn tắm Jacuzzi")
        );
        Map<String, String> typeImages = Map.of(
            "Deluxe City View", "/images/deluxe-city-view.jpg",
            "Deluxe Triple City View", "/images/duplex1.jpg",
            "Executive Suite City View", "/images/executive-suite-city-view.jpg",
            "Executive Twin City View", "/images/executive-twin-city-view.jpg",
            "Presidential Suite", "/images/presidential-suite.jpg"
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
            summary.put("image", typeImages.getOrDefault(type, "/images/deluxe-city-view.jpg"));
            summary.put("minPrice", minPrice);
            summary.put("maxPrice", maxPrice);
            summary.put("priceDisplay", minPrice == maxPrice
                ? formatPrice(minPrice)
                : formatPrice(minPrice) + " - " + formatPrice(maxPrice));
            summary.put("availableCount", availableCount);
            summary.put("totalCount", typeRooms.size());
            summary.put("maxCapacity", maxCapacity);
            summary.put("idSafe", type.replaceAll("\\s+", "-"));

            roomTypeSummaries.add(summary);

            Map<String, Object> typeOption = new LinkedHashMap<>();
            typeOption.put("type", type);
            typeOption.put("price", formatPrice(minPrice));
            roomTypesList.add(typeOption);
        }

        // Build roomsByType: Map of room type -> list of actual Room objects
        Map<String, List<Room>> roomsByType = new LinkedHashMap<>();
        Map<String, Long> availableRoomCounts = new HashMap<>();
        Map<String, Boolean> roomAvailabilityMap = new HashMap<>();

        for (String type : allTypes) {
            List<Room> typeRooms = repo.findRoomsByType(type);
            if (!typeRooms.isEmpty()) {
                roomsByType.put(type, typeRooms);
                long count = 0;
                for (Room room : typeRooms) {
                    boolean available = true;
                    if (isSearched) {
                        available = !repo.isRoomBookedInPeriod(room.getRoomId(), checkInDate, checkOutDate) 
                                    && room.getStatus() != RoomStatus.MAINTENANCE 
                                    && room.getStatus() != RoomStatus.OUT_OF_ORDER;
                    }
                    roomAvailabilityMap.put(room.getRoomId(), available);
                    if (available) {
                        count++;
                    }
                }
                availableRoomCounts.put(type, count);
            }
        }
        
        model.addAttribute("roomTypeSummaries", roomTypeSummaries);
        model.addAttribute("roomTypesForSelect", roomTypesList);
        model.addAttribute("roomsByType", roomsByType);
        model.addAttribute("availableRoomCounts", availableRoomCounts);
        model.addAttribute("roomAvailabilityMap", roomAvailabilityMap);
        model.addAttribute("typeImages", typeImages);
        return "index";
    }

    private String formatPrice(double price) {
        if (price >= 1_000_000) {
            return String.format("%.1f tr", price / 1_000_000);
        }
        return String.format("%,.0fđ", price);
    }

    private List<String> getBookingRoomTypes() {
        return List.of(
            "Deluxe City View",
            "Deluxe Triple City View",
            "Executive Suite City View",
            "Executive Twin City View",
            "Presidential Suite"
        );
    }

    private List<String> getPaymentMethods() {
        return List.of("Tiền mặt", "Chuyển khoản", "Thẻ tín dụng", "Ví điện tử");
    }

    private List<String> getBookingSources() {
        return List.of("Gặp trực tiếp", "Điện thoại", "Fax", "Email", "Internet");
    }

    @ModelAttribute
    public void addCurrentGuestToModel(HttpSession session, Model model) {
        String guestId = (String) session.getAttribute("loggedGuestId");
        if (guestId != null) {
            repo.findGuestById(guestId).ifPresent(guest -> model.addAttribute("currentGuest", guest));
        }
    }

    private Optional<Guest> getLoggedGuest(HttpSession session) {
        String guestId = (String) session.getAttribute("loggedGuestId");
        return guestId == null ? Optional.empty() : repo.findGuestById(guestId);
    }

    @GetMapping("/step1")
    public String step1(@RequestParam(required = false) String roomType, Model model, HttpSession session) {
        List<String> roomTypes = getBookingRoomTypes();
        repo.findGuestById((String) session.getAttribute("loggedGuestId")).ifPresent(guest -> {
            model.addAttribute("guestName", guest.getFullName());
            model.addAttribute("phone", guest.getPhone());
            model.addAttribute("email", guest.getEmail());
            model.addAttribute("address", guest.getAddress());
        });
        model.addAttribute("roomTypes", roomTypes);
        model.addAttribute("paymentMethods", getPaymentMethods());
        model.addAttribute("bookingSources", getBookingSources());
        model.addAttribute("selectedRoomType", roomType == null ? roomTypes.get(0) : roomType);
        model.addAttribute("checkInDefault", LocalDate.now().plusDays(1).toString());
        model.addAttribute("checkOutDefault", LocalDate.now().plusDays(3).toString());
        return "step1";
    }

    @GetMapping("/introduction")
    public String introduction() {
        return "introduction";
    }

    @GetMapping("/booking")
    public String booking(Model model) {
        model.addAttribute("rooms", repo.getAllRooms());
        model.addAttribute("totalRooms", repo.countTotalRooms());
        model.addAttribute("availableRooms", repo.countAvailableRooms());
        return "booking";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
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

        // Kiểm tra danh sách đen (Blacklist)
        Optional<Guest> existingGuest = repo.findGuestByPhone(phone);
        if (existingGuest.isPresent() && existingGuest.get().isBlacklisted()) {
            model.addAttribute("error", "Tài khoản/Số điện thoại này hiện đang bị tạm khóa (Blacklist). Lý do: " + existingGuest.get().getBlacklistReason());
            model.addAttribute("roomTypes", getBookingRoomTypes());
            model.addAttribute("paymentMethods", getPaymentMethods());
            model.addAttribute("bookingSources", getBookingSources());
            model.addAttribute("selectedRoomType", roomType);
            model.addAttribute("checkIn", checkIn);
            model.addAttribute("checkOut", checkOut);
            model.addAttribute("numberOfGuests", numberOfGuests);
            model.addAttribute("numberOfRooms", numberOfRooms);
            model.addAttribute("paymentMethod", paymentMethod);
            model.addAttribute("reservationType", reservationType);
            model.addAttribute("bookingSource", bookingSource);
            model.addAttribute("guestName", guestName);
            model.addAttribute("phone", phone);
            model.addAttribute("email", email);
            model.addAttribute("address", address);
            model.addAttribute("fax", fax);
            model.addAttribute("specialRequests", specialRequests);
            return "step1";
        }

        List<Room> availableRooms = repo.findAvailableRoomsForPeriod(roomType, numberOfGuests, checkInDate, checkOutDate);

        if (availableRooms.size() < numberOfRooms) {
            model.addAttribute("error", "Không có đủ " + numberOfRooms + " phòng trống cho yêu cầu này!");
            model.addAttribute("roomTypes", getBookingRoomTypes());
            model.addAttribute("paymentMethods", getPaymentMethods());
            model.addAttribute("bookingSources", getBookingSources());
            model.addAttribute("selectedRoomType", roomType);
            model.addAttribute("checkIn", checkIn);
            model.addAttribute("checkOut", checkOut);
            model.addAttribute("numberOfGuests", numberOfGuests);
            model.addAttribute("numberOfRooms", numberOfRooms);
            model.addAttribute("paymentMethod", paymentMethod);
            model.addAttribute("reservationType", reservationType);
            model.addAttribute("bookingSource", bookingSource);
            model.addAttribute("guestName", guestName);
            model.addAttribute("phone", phone);
            model.addAttribute("email", email);
            model.addAttribute("address", address);
            model.addAttribute("fax", fax);
            model.addAttribute("specialRequests", specialRequests);

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
        model.addAttribute("totalPrice", selectedRoom.getPricePerNight() * nights * numberOfRooms);

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
            Model model,
            HttpSession session) {

        LocalDate checkInDate = LocalDate.parse(checkIn);
        LocalDate checkOutDate = LocalDate.parse(checkOut);
        int nights = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);

        Guest guest = getLoggedGuest(session).orElse(null);
        if (guest == null) {
            guest = new Guest(guestName, phone, email, address);
        } else {
            guest.setFullName(guestName);
            guest.setPhone(phone);
            guest.setEmail(email);
            guest.setAddress(address);
        }
        guest.setRegistrantName(registrantName);
        guest.setFax(fax);

        repo.saveGuest(guest);
        dbManager.saveGuest(guest);

        String[] roomIds = roomId.split(",");
        Room firstRoom = repo.findRoomById(roomIds[0]).orElseThrow();
        ReservationType resType = reservationType.equals("Đảm bảo") ? ReservationType.GUARANTEED : ReservationType.NON_GUARANTEED;
        BookingSource source = BookingSource.valueOf(bookingSource.toUpperCase().replace(" ", "_"));

        double totalPrice = firstRoom.getPricePerNight() * nights * roomIds.length;

        if (resType == ReservationType.GUARANTEED) {
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
            model.addAttribute("room", firstRoom);
            model.addAttribute("nights", nights);
            model.addAttribute("numberOfRooms", roomIds.length);
            model.addAttribute("totalPrice", totalPrice);

            return "payment";
        }

        repo.saveGuest(guest);
        dbManager.saveGuest(guest);

        Reservation lastReservation = null;
        for (String rId : roomIds) {
            Room r = repo.findRoomById(rId).orElseThrow();
            lastReservation = repo.createReservation(
                    guest, r, checkInDate, checkOutDate,
                    numberOfGuests, resType, source,
                    paymentMethod, specialRequests != null ? specialRequests : ""
            );
            repo.updateRoomStatus(rId, RoomStatus.OCCUPIED);
            dbManager.saveRoom(r);
            dbManager.saveReservation(lastReservation);
        }

        model.addAttribute("reservation", lastReservation);
        model.addAttribute("guest", guest);
        model.addAttribute("room", firstRoom);
        model.addAttribute("nights", nights);
        model.addAttribute("numberOfRooms", roomIds.length);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("displayRoomId", roomId);

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
            Model model,
            HttpSession session) {

        LocalDate checkInDate = LocalDate.parse(checkIn);
        LocalDate checkOutDate = LocalDate.parse(checkOut);
        int nights = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);

        Guest guest = getLoggedGuest(session).orElse(null);
        if (guest == null) {
            guest = new Guest(guestName, phone, email, address);
        } else {
            guest.setFullName(guestName);
            guest.setPhone(phone);
            guest.setEmail(email);
            guest.setAddress(address);
        }
        guest.setRegistrantName(registrantName);
        guest.setFax(fax);
        repo.saveGuest(guest);
        dbManager.saveGuest(guest);

        String[] roomIds = roomId.split(",");
        Room firstRoom = repo.findRoomById(roomIds[0]).orElseThrow();
        ReservationType resType = reservationType.equals("Đảm bảo") ? ReservationType.GUARANTEED : ReservationType.NON_GUARANTEED;
        BookingSource source = BookingSource.valueOf(bookingSource.toUpperCase().replace(" ", "_"));

        double totalPrice = firstRoom.getPricePerNight() * nights * roomIds.length;
        String paymentDetail = paymentMethod + " (Đã cọc 50%: " + String.format("%,.0f", totalPrice / 2) + " VND, Mã GD: " + transactionCode + ")";

        Reservation lastReservation = null;
        for (String rId : roomIds) {
            Room r = repo.findRoomById(rId).orElseThrow();
            lastReservation = repo.createReservation(
                    guest, r, checkInDate, checkOutDate,
                    numberOfGuests, resType, source,
                    paymentDetail, specialRequests
            );
            repo.updateRoomStatus(rId, RoomStatus.OCCUPIED);
            dbManager.saveRoom(r);
            dbManager.saveReservation(lastReservation);
        }

        model.addAttribute("reservation", lastReservation);
        model.addAttribute("guest", guest);
        model.addAttribute("room", firstRoom);
        model.addAttribute("nights", nights);
        model.addAttribute("numberOfRooms", roomIds.length);
        model.addAttribute("totalPrice", firstRoom.getPricePerNight() * nights * roomIds.length);
        model.addAttribute("transactionCode", transactionCode);
        model.addAttribute("displayRoomId", roomId);

        return "success";
    }

    @GetMapping("/user/login")
    public String userLogin(HttpSession session) {
        if (session.getAttribute("loggedGuestId") != null) {
            return "redirect:/user/history";
        }
        return "user_login";
    }

    @PostMapping("/user/login")
    public String doUserLogin(
            @RequestParam String phone,
            @RequestParam String email,
            HttpSession session,
            Model model) {

        Optional<Guest> guest = repo.findGuestByPhone(phone)
                .filter(g -> g.getEmail() != null && g.getEmail().equalsIgnoreCase(email));

        if (guest.isPresent()) {
            session.setAttribute("loggedGuestId", guest.get().getGuestId());
            return "redirect:/user/history";
        }

        model.addAttribute("error", "Số điện thoại hoặc email không đúng hoặc chưa đăng ký.");
        return "user_login";
    }

    @GetMapping("/user/register")
    public String userRegister(HttpSession session) {
        if (session.getAttribute("loggedGuestId") != null) {
            return "redirect:/user/history";
        }
        return "user_register";
    }

    @PostMapping("/user/register")
    public String doUserRegister(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam String address,
            HttpSession session,
            Model model) {

        if (repo.findGuestByPhone(phone).isPresent()) {
            model.addAttribute("error", "Số điện thoại này đã được đăng ký. Vui lòng đăng nhập hoặc dùng số khác.");
            model.addAttribute("fullName", fullName);
            model.addAttribute("phone", phone);
            model.addAttribute("email", email);
            model.addAttribute("address", address);
            return "user_register";
        }

        Guest guest = new Guest(fullName, phone, email, address);
        repo.saveGuest(guest);
        dbManager.saveGuest(guest);
        session.setAttribute("loggedGuestId", guest.getGuestId());
        return "redirect:/user/history";
    }

    @GetMapping("/user/logout")
    public String userLogout(HttpSession session) {
        session.removeAttribute("loggedGuestId");
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/user/history")
    public String userHistory(HttpSession session, Model model) {
        Optional<Guest> guest = getLoggedGuest(session);
        if (guest.isEmpty()) {
            return "redirect:/user/login";
        }

        List<Reservation> reservations = repo.getAllReservations().stream()
                .filter(r -> r.getGuest().getGuestId().equals(guest.get().getGuestId()))
                .toList();

        model.addAttribute("reservations", reservations);
        model.addAttribute("guest", guest.get());
        return "user_history";
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

    // ═══════════════════════════════════════════════════════════════
    // ĐƯỜNG DẪN QUẢN TRỊ (ADMIN ENDPOINTS)
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/admin/login")
    public String adminLogin(HttpSession session) {
        if (session.getAttribute("adminUser") != null) {
            return "redirect:/admin";
        }
        return "admin_login";
    }

    @PostMapping("/admin/login")
    public String doAdminLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        if ("admin".equals(username) && "admin123".equals(password)) {
            session.setAttribute("adminUser", username);
            return "redirect:/admin";
        }
        model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng!");
        return "admin_login";
    }

    @GetMapping("/admin/logout")
    public String adminLogout(HttpSession session) {
        session.removeAttribute("adminUser");
        session.invalidate();
        return "redirect:/admin/login";
    }

    @GetMapping("/admin")
    public String adminDashboard(HttpSession session, Model model) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/admin/login";
        }

        List<Room> allRooms = repo.getAllRooms();
        List<Reservation> allReservations = repo.getAllReservations();
        List<Guest> allGuests = repo.getAllGuests();

        // Tính toán số liệu thống kê
        int totalRooms = allRooms.size();
        long occupiedCount = allRooms.stream()
                .filter(r -> r.getStatus() == RoomStatus.OCCUPIED)
                .count();
        double occupancyRate = totalRooms > 0 ? (double) occupiedCount / totalRooms * 100 : 0;

        long todayReservationsCount = allReservations.stream()
                .filter(res -> res.getCreatedAt().toLocalDate().equals(LocalDate.now()))
                .count();

        long blacklistCount = allGuests.stream()
                .filter(Guest::isBlacklisted)
                .count();

        model.addAttribute("rooms", allRooms);
        model.addAttribute("reservations", allReservations);
        model.addAttribute("guests", allGuests);

        Map<String, String> roomOccupiedPeriods = new HashMap<>();
        for (Room room : allRooms) {
            if (room.getStatus() == RoomStatus.OCCUPIED) {
                repo.findActiveReservationForRoom(room.getRoomId())
                    .ifPresent(res -> roomOccupiedPeriods.put(
                        room.getRoomId(),
                        "Có khách ngày " + res.getCheckInDate() + " đến ngày " + res.getCheckOutDate()
                    ));
            }
        }
        model.addAttribute("roomOccupiedPeriods", roomOccupiedPeriods);
        
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("occupiedRooms", occupiedCount);
        model.addAttribute("occupancyRate", occupancyRate);
        model.addAttribute("todayReservations", todayReservationsCount);
        model.addAttribute("blacklistCount", blacklistCount);

        model.addAttribute("roomStatuses", RoomStatus.values());
        model.addAttribute("reservationStatuses", ReservationStatus.values());
        model.addAttribute("bookingSources", BookingSource.values());
        model.addAttribute("reservationTypes", ReservationType.values());

        return "admin";
    }

    @PostMapping("/admin/rooms/add")
    public String addRoom(
            @RequestParam String roomId,
            @RequestParam String roomType,
            @RequestParam double pricePerNight,
            @RequestParam int floor,
            @RequestParam int capacity,
            @RequestParam String status,
            @RequestParam(required = false) String description,
            HttpSession session) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/admin/login";
        }

        Room newRoom = new Room(roomId, roomType, pricePerNight, floor, capacity);
        newRoom.setStatus(RoomStatus.valueOf(status));
        newRoom.setDescription(description != null ? description : "");
        
        repo.addRoom(newRoom);
        dbManager.saveRoom(newRoom);

        return "redirect:/admin?tab=rooms";
    }

    @PostMapping("/admin/rooms/edit")
    public String editRoom(
            @RequestParam String roomId,
            @RequestParam String roomType,
            @RequestParam double pricePerNight,
            @RequestParam int floor,
            @RequestParam int capacity,
            @RequestParam String status,
            @RequestParam(required = false) String description,
            HttpSession session) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/admin/login";
        }

        Room updatedRoom = new Room(roomId, roomType, pricePerNight, floor, capacity);
        updatedRoom.setStatus(RoomStatus.valueOf(status));
        updatedRoom.setDescription(description != null ? description : "");

        repo.addOrUpdateRoom(updatedRoom);
        dbManager.saveRoom(updatedRoom);

        return "redirect:/admin?tab=rooms";
    }

    @PostMapping("/admin/rooms/status")
    public String updateRoomStatus(
            @RequestParam String roomId,
            @RequestParam String status,
            HttpSession session) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/admin/login";
        }

        repo.updateRoomStatus(roomId, RoomStatus.valueOf(status));
        repo.findRoomById(roomId).ifPresent(dbManager::saveRoom);

        return "redirect:/admin?tab=rooms";
    }

    @PostMapping("/admin/reservations/update-status")
    public String updateReservationStatus(
            @RequestParam String reservationId,
            @RequestParam String status,
            @RequestParam(required = false) String cancelReason,
            HttpSession session) {
        String adminUser = (String) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }

        repo.findReservationById(reservationId).ifPresent(res -> {
            ReservationStatus newStatus = ReservationStatus.valueOf(status);
            
            if (newStatus == ReservationStatus.CONFIRMED) {
                res.confirm(adminUser);
            } else if (newStatus == ReservationStatus.CHECKED_IN) {
                res.checkIn(adminUser);
                repo.updateRoomStatus(res.getRoom().getRoomId(), RoomStatus.OCCUPIED);
                dbManager.saveRoom(res.getRoom());
            } else if (newStatus == ReservationStatus.CHECKED_OUT) {
                res.checkOut(adminUser);
                repo.updateRoomStatus(res.getRoom().getRoomId(), RoomStatus.AVAILABLE);
                dbManager.saveRoom(res.getRoom());
            } else if (newStatus == ReservationStatus.CANCELLED) {
                res.cancel(cancelReason != null && !cancelReason.isEmpty() ? cancelReason : "Hủy bởi Admin", adminUser);
                repo.updateRoomStatus(res.getRoom().getRoomId(), RoomStatus.AVAILABLE);
                dbManager.saveRoom(res.getRoom());
            } else if (newStatus == ReservationStatus.NO_SHOW) {
                res.markNoShow(adminUser);
                repo.updateRoomStatus(res.getRoom().getRoomId(), RoomStatus.AVAILABLE);
                dbManager.saveRoom(res.getRoom());
            } else {
                res.setStatus(newStatus);
            }
            dbManager.saveReservation(res);
        });

        return "redirect:/admin?tab=reservations";
    }

    @PostMapping("/admin/guests/blacklist")
    public String toggleGuestBlacklist(
            @RequestParam String guestId,
            @RequestParam(required = false) String reason,
            HttpSession session) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/admin/login";
        }

        repo.findGuestById(guestId).ifPresent(g -> {
            if (g.isBlacklisted()) {
                g.removeFromBlacklist();
            } else {
                g.addToBlacklist(reason != null && !reason.isEmpty() ? reason : "Lý do khác");
            }
            dbManager.saveGuest(g);
        });

        return "redirect:/admin?tab=guests";
    }

    @PostMapping("/admin/guests/edit")
    public String editGuest(
            @RequestParam String guestId,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam String address,
            @RequestParam String idNumber,
            HttpSession session) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/admin/login";
        }

        repo.findGuestById(guestId).ifPresent(g -> {
            g.setFullName(fullName);
            g.setPhone(phone);
            g.setEmail(email);
            g.setAddress(address);
            g.setIdNumber(idNumber);
            
            repo.updateGuest(g);
            dbManager.saveGuest(g);
        });

        return "redirect:/admin?tab=guests";
    }
}
