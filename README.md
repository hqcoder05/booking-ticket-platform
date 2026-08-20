# Hệ thống Đặt vé Sự kiện (Concert Ticket Booking Platform)

Đây là dự án backend phục vụ cho bài Assessment vị trí Product Backend Engineer. Hệ thống cung cấp các RESTful API an toàn, xử lý concurrency tốt, phục vụ cho hai luồng nghiệp vụ chính: Khách hàng (Customer booking workflows) và Vận hành nội bộ (Internal operation workflows).

## 1. Hướng dẫn Cài đặt & Chạy dự án Local

### Yêu cầu môi trường (Prerequisites)
- Java 21
- Maven
- Docker & Docker Compose

### Khởi chạy Ứng dụng
1. **Khởi động Database (PostgreSQL):**
   ```bash
   docker-compose up -d
   ```
2. **Chạy ứng dụng Spring Boot:**
   ```bash
   ./mvnw spring-boot:run
   ```
   *Lưu ý: Flyway sẽ tự động tạo bảng (schema). Sau đó, `DataSeeder` (CommandLineRunner) sẽ kiểm tra và tự động seed dữ liệu mẫu nếu database trống (users, venues, concerts, seats, vouchers). Cơ chế này đảm bảo dữ liệu luôn được khôi phục dù bạn restart server bao nhiêu lần.*

### Tài khoản Test có sẵn trong hệ thống

| Email               | Mật khẩu    | Role       | Mô tả                           |
|---------------------|-------------|------------|----------------------------------|
| `admin@geekup.vn`   | `admin123`  | `ADMIN`    | Toàn quyền quản trị hệ thống    |
| `operator@geekup.vn`| `admin123`  | `OPERATOR` | Quản lý sự kiện và đơn hàng     |
| `customer@geekup.vn`| `admin123`  | `CUSTOMER` | Khách hàng đặt vé               |

### Dữ liệu mẫu có sẵn

**Venues (Địa điểm):**
- Sân vận động Quốc gia Mỹ Đình (Hà Nội - 40.000 chỗ)
- Nhà hát Hòa Bình (TP.HCM - 2.500 chỗ)
- Phú Thọ Indoor Stadium (TP.HCM - 5.000 chỗ)

**Concerts (Sự kiện):**
- 🎤 **FC Bayern Munich x BTS - World Tour 2027** (PUBLISHED - sẵn sàng mở bán)
- 🎤 **BLACKPINK - Born Pink World Tour Finale** (PUBLISHED)
- 🎤 **Sơn Tùng M-TP - Sky Tour 2027** (DRAFT - chưa mở bán)

**Vouchers (Mã giảm giá):**
| Mã          | Loại       | Giá trị  | Giới hạn |
|-------------|------------|----------|----------|
| `EARLYBIRD` | Giảm %     | 10%      | 100 lượt |
| `FLASHSALE50K` | Giảm tiền | 50.000đ | 200 lượt |
| `VIP20`     | Giảm %     | 20%      | 50 lượt  |
| `GEEKUP100K`| Giảm tiền  | 100.000đ | 30 lượt  |

## 2. Tài liệu API & Bộ kiểm thử (API Documents & Testing)

### Swagger OpenAPI
Sau khi ứng dụng chạy thành công, tài liệu API tương tác trực tiếp có thể truy cập tại:
- **URL:** http://localhost:8080/swagger-ui.html

### Postman Collection
Bộ API testing collection đã được đính kèm ở thư mục gốc của dự án: `postman_collection.json`.
- **Cách sử dụng:** Import file này vào Postman.
- **Tự động hóa:** Collection đã được setup sẵn các Pre-request scripts. Khi bạn gọi API Login, nó sẽ tự động lấy JWT Token và gán vào biến môi trường `{{token}}` cho các request tiếp theo.

## 3. Quy chuẩn Code (Coding Guideline & Convention)

### Cấu trúc dự án (Code Structure)
Mã nguồn tuân theo kiến trúc Domain-Driven Monolith, phân tách rạch ròi theo luồng nghiệp vụ:
- `auth`: Đăng ký, đăng nhập, xác thực JWT.
- `concert`: Quản lý sự kiện, venue, ticket categories, seats.
- `booking`: Luồng đặt vé, áp dụng voucher, quản lý trạng thái.
- `payment`: Thanh toán và quản lý voucher.
- `shared`: Config, exceptions, security, base DTOs dùng chung.

### Hướng dẫn tạo API mới (How to Code a New API)
1. **Controller Layer:** Định nghĩa endpoint tại package nghiệp vụ tương ứng. Sử dụng `@PreAuthorize` để phân quyền Role-Based Access Control. Luôn trả về dữ liệu được bọc trong class `ApiResponse<T>`.
2. **DTO Layer:** Tạo các class Request và Response DTO cụ thể. Tuyệt đối không expose raw Entities ra ngoài API.
3. **Service Layer:** Triển khai business logic tại đây. Áp dụng `@Transactional` để gom nhóm các thao tác. Nếu API có làm thay đổi trạng thái nhạy cảm (như inventory ghế), bắt buộc dùng Pessimistic Locks ở tầng Repository.
4. **Repository Layer:** Sử dụng Spring Data JPA. Giữ các câu query được tối ưu hóa.

### Hướng dẫn viết và chạy Unit Test
- **Frameworks:** JUnit 5, Mockito.
- **Lệnh chạy test:**
  ```bash
  ./mvnw test
  ```
- **Convention:** Viết unit test cho các business logic (Services) và viết integration test cho các vấn đề concurrency (sử dụng `ExecutorService` và `CountDownLatch` để giả lập nhiều threads cùng truy cập vào một resource). Tham khảo file `BookingServiceConcurrencyTest.java`.

### Chạy Load Test
- **Yêu cầu:** Python 3.x
- **Lệnh chạy:**
  ```bash
  python load_test.py
  ```
  Script sẽ bắn 10.000 requests đồng thời vào API bản đồ ghế để kiểm chứng hiệu quả của Caffeine Cache.

## 4. Thiết kế Hệ thống & Các Giả định
Vui lòng tham khảo tài liệu chi tiết tại **[system-design.md](./system-design.md)**. Tài liệu này sẽ trả lời các yêu cầu cốt lõi của bài test:
- Tư duy thiết kế Backend.
- Cách giải quyết các vấn đề hóc búa (Overselling, Duplicates, Flash Sale).
- Các giả định, phạm vi những gì ĐÃ LÀM (In scope) và KHÔNG LÀM (Out of scope).

## 5. Luồng nghiệp vụ chính (Core Workflows)

### Luồng Khách hàng đặt vé (Customer Booking Flow)
```
1. Xem danh sách Concert (PUBLISHED) → GET /api/customer/concerts
2. Xem bản đồ ghế trống          → GET /api/customer/concerts/{id}/seats
3. Đặt vé (chọn ghế + voucher)    → POST /api/customer/bookings
4. Thanh toán                      → POST /api/customer/bookings/{id}/pay
5. Xem lịch sử đặt vé             → GET /api/customer/bookings
```

### Luồng Vận hành nội bộ (Operation Flow)
```
1. Tạo / Quản lý Venue            → CRUD /api/operation/venues
2. Tạo / Quản lý Concert          → CRUD /api/operation/concerts
3. Quản lý Ticket Category & Seat → API nested trong Concert
4. Xem tất cả Booking             → GET /api/operation/bookings
5. Cập nhật trạng thái Booking     → PUT /api/operation/bookings/{id}/status
```