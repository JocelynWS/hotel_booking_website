# Hotel Booking System 🏨
**Nhóm 8 – Lập Trình Nâng Cao**

| Thành viên | Vai trò |
|---|---|
| Trần Nguyễn Hà Lan | Leader |
| Ngô Thu Thảo | Member |
| Lưu Trí Dũng | Member |
| Bùi Nam Dương | Member |

---

## Cấu trúc dự án

```
hotel-booking-system/
├── src/
│   ├── main/java/com/hotel/
│   │   ├── model/          ← Data Layer (Tuần 5)
│   │   │   ├── Employee.java
│   │   │   ├── EmployeeRole.java
│   │   │   ├── Guest.java
│   │   │   ├── Room.java
│   │   │   ├── RoomStatus.java
│   │   │   ├── Reservation.java
│   │   │   │     └── (inner) ReservationHistory
│   │   │   ├── ReservationStatus.java
│   │   │   ├── ReservationType.java
│   │   │   ├── WaitingEntry.java
│   │   │   └── BookingSource.java
│   │   ├── service/        ← Business Logic Layer (Tuần 6) ✅
│   │   │   ├── Result.java
│   │   │   ├── ReservationService.java
│   │   │   ├── RoomService.java
│   │   │   ├── GuestService.java
│   │   │   └── WaitingListService.java
│   │   ├── exception/      ← Custom Exceptions (Tuần 6)
│   │   │   ├── HotelException.java
│   │   │   ├── RoomNotAvailableException.java
│   │   │   ├── GuestBlacklistedException.java
│   │   │   ├── ReservationNotFoundException.java
│   │   │   ├── InvalidOperationException.java
│   │   │   └── UnauthorizedOperationException.java
│   │   ├── repository/     ← Data Access Layer (Tuần 7) ✅
│   │   │   ├── Repository.java (interface)
│   │   │   ├── JsonRepository.java (base impl)
│   │   │   ├── RoomRepository.java
│   │   │   ├── GuestRepository.java
│   │   │   ├── EmployeeRepository.java
│   │   │   ├── ReservationRepository.java
│   │   │   ├── WaitingEntryRepository.java
│   │   │   └── DatabaseService.java (facade)
│   │   └── Main.java
│   └── test/java/com/hotel/
│       └── model/          ← Unit Tests
├── pom.xml
└── README.md
```

---

## Tiến độ

| Tuần | Nội dung | Trạng thái |
|---|---|---|
| 1–2 | Ghép nhóm, chọn đề tài | ✅ Hoàn thành |
| 3 | Xác định chu trình nghiệp vụ + scope | ✅ Hoàn thành |
| 4 | Đặc tả yêu cầu + diagrams | ✅ Hoàn thành |
| 5 | Models & Data Layer | ✅ Hoàn thành |
| 6 | Service Layer | ✅ Hoàn thành |
| 7 | Repository Layer | ✅ Hoàn thành |
| 8+ | UI Layer | 🔄 Tiếp theo |

---

## Yêu cầu

- Java 17+
- Maven 3.8+

## Chạy dự án

```bash
mvn compile
mvn test
```
