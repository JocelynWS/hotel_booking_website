# Hotel Booking System 🏨
**Nhóm 8 – Lập Trình Nâng Cao**

## Thành viên
| Họ tên |
|--------|
| Trần Nguyễn Hà Lan |
| Ngô Thu Thảo |
| Lưu Trí Dũng |
| Bùi Nam Dương |

---

## Cấu trúc dự án

```
hotel-booking-system/
├── src/main/java/com/hotel/
│   ├── HotelBookingApplication.java
│   ├── model/
│   ├── repository/
│   │   └── HotelRepository.java
│   └── controller/
│       └── BookingController.java
├── src/main/resources/
│   ├── templates/
│   │   ├── index.html
│   │   ├── step1.html
│   │   ├── step1_confirm.html
│   │   ├── step2.html
│   │   ├── step2_result.html
│   │   └── success.html
│   └── application.properties
└── pom.xml
```

---

## Tiến độ (Theo 8 bước Scope)

| Bước | Nội dung | Trạng thái |
|------|----------|------------|
| **1** | Nhận yêu cầu đặt buồng | ✅ Hoàn thành |
| **2** | Xác định khả năng đáp ứng | ✅ Hoàn thành |
| **3** | Thoả thuận & thuyết phục | ✅ Hoàn thành |
| **4** | Nhập thông tin đặt buồng | ✅ Hoàn thành |
| **5** | Xác nhận đặt buồng | ✅ Hoàn thành |
| 6 | Lưu thông tin | 🔜 Tiếp theo |
| 7 | Sửa / Hủy | 🔜 Tiếp theo |
| 8 | Chuyển bộ phận đón tiếp | ⏭️ Bỏ qua |

---

## Yêu cầu
- Java 17+
- Maven 3.8+

## Chạy dự án

```bash
cd hotel-booking-system
mvn spring-boot:run
```

Sau đó mở: **http://localhost:8080**

---

## Các trang

| Trang | URL | Mô tả |
|-------|-----|-------|
| Trang chủ | `/` | Dashboard + danh sách phòng |
| Đặt phòng | `/step1` | Form nhận yêu cầu |
| Kiểm tra | `/step2` | Tìm phòng trống + gợi ý |
| Kết quả | `/step2/check` | Phân tích + phòng trống |
| Xác nhận | `/step1/submit` | Chọn phòng + đặt |
| Thành công | `/step1/book` | Thông báo đặt thành công |
