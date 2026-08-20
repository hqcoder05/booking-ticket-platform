# Tài liệu Thiết kế Hệ thống & Cơ sở dữ liệu

Tài liệu này trình bày các quyết định về kiến trúc, sự đánh đổi (trade-offs), và tư duy kỹ thuật đằng sau hệ thống Concert Ticket Booking Platform.

## 1. Tư duy Thiết kế Backend (How I Think About Backend Design)

Cách tiếp cận của tôi đối với hệ thống này đặt **Tính toàn vẹn dữ liệu (Data Integrity), Sự tối giản (Simplicity), và Khả năng bảo vệ thiết kế (Defensibility)** lên hàng đầu thay vì tối ưu hóa viển vông (premature optimization). Với ngữ cảnh giới hạn 48 giờ và startup chuẩn bị chạy Flash Sale, triết lý của tôi là:
- **Monolith First:** Một kiến trúc Monolith được module hóa rõ ràng (phân tách rạch ròi domain `customer` và `operation`) mang lại giá trị cao hơn nhiều so với một kiến trúc Microservices làm vội vàng. Nó loại bỏ độ trễ mạng (network latency) giữa các services và giúp việc quản lý ACID transactions trở nên an toàn tuyệt đối.
- **Let the Database Do the Heavy Lifting:** PostgreSQL là một hệ quản trị CSDL cực kỳ mạnh mẽ. Thay vì nhồi nhét các cơ chế distributed locks từ bên ngoài (như Redis/Zookeeper) làm tăng độ phức tạp vận hành, tôi tận dụng triệt để khóa mức dòng (Row-level locking: `SELECT FOR UPDATE`) của PostgreSQL để đảm bảo tính nhất quán dữ liệu.

## 2. Thiết kế Cơ sở dữ liệu (Database Design)

Database được chuẩn hóa (normalized) hoàn toàn và quản lý version qua **Flyway Migrations**.
- **Sử dụng UUID làm Primary Keys:** Chống lại các rủi ro bảo mật (ID enumeration attacks) và dọn đường sẵn cho việc phân tán dữ liệu (horizontal partitioning) trong tương lai.
- **Core Entities:** `User`, `Concert`, `Venue`, `Seat` (Inventory), `Booking`, `BookingDetail`, `Payment`, `Voucher`.
- **Hỗ trợ Concurrency:** Các bảng `Seat` và `Voucher` được xác định là "nút thắt cổ chai" (bottlenecks) trong đợt flash sale. Chúng được thiết kế để có thể khóa (lock) an toàn trong các transactions.

### Mô hình Dữ liệu (ERD)
```
User (1) ──── (N) Booking (N) ──── (1) Concert (N) ──── (1) Venue
                    │                     │
                    │                     └──── (N) TicketCategory ──── (N) Seat
                    │
                    ├──── (N) BookingDetail
                    ├──── (1) Payment
                    └──── (0..1) Voucher
```

### Dữ liệu mẫu đã được Seed
Hệ thống sử dụng Flyway migration `V4__seed_all_data.sql` để tạo sẵn:
- **3 tài khoản test** (ADMIN, OPERATOR, CUSTOMER) - tất cả dùng mật khẩu `admin123`
- **3 venues** thực tế tại Việt Nam (Mỹ Đình, Nhà hát Hòa Bình, Phú Thọ)
- **3 concerts** (FC Bayern Munich x BTS, BLACKPINK, Sơn Tùng M-TP)
- **2.800+ ghế ngồi** được seed tự động qua `generate_series()`
- **4 vouchers** với 2 loại giảm giá (Percentage và Fixed Amount)

## 3. Giới hạn Phạm vi & Các Giả định (Assumptions & Scope)

Để mang lại giá trị cốt lõi nhất trong thời gian giới hạn, tôi đã khoanh vùng phạm vi dự án một cách cẩn thận.

### Những tính năng ĐÃ LÀM (In Scope):
- **Máy trạng thái 4-bước cho Booking:** `PENDING` → `PAID` → `CANCELLED` → `REFUNDED`
- **Áp dụng Voucher khi đặt vé:** Khách hàng gửi `voucherCode` trong request đặt vé. Hệ thống tự động tính giảm giá (theo % hoặc theo số tiền cố định) và áp dụng Pessimistic Lock để chống lạm dụng voucher.
- **Idempotent API:** Hỗ trợ safe retries cho luồng Booking.
- **Seat Auto-Release:** Scheduled Job quét và nhả ghế `PENDING` nếu không thanh toán trong 5 phút.
- **Anti-Seat Hoarding:** 1 User chỉ được phép có tối đa 1 booking `PENDING` cùng lúc.
- **In-Memory Cache:** Caffeine Cache với TTL 2 giây cho API bản đồ ghế, bảo vệ Database khỏi 50.000+ users polling liên tục.
- **Load Test thực tế:** Script Python bắn 10.000 requests đồng thời, chứng minh hệ thống đạt 500+ RPS trên máy local.

### Những tính năng CHƯA LÀM (Out of Scope / Limitations):
- **CRUD Operations cho Vouchers:** Hệ thống *KHÔNG* cung cấp các API cho Operation để create/update/delete vouchers. Dữ liệu voucher được seed qua Flyway.
- **Tích hợp Cổng thanh toán thật:** API `/pay` mock quá trình thanh toán thành công.
- **WebSockets cho Bản đồ ghế:** Frontend dùng cơ chế Polling.

## 4. Giải quyết các Vấn đề & Rủi ro Cốt lõi

### A. Bán lố vé (Overselling)
- **Giải pháp:** `Pessimistic Locking` (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) khi SELECT Seat. Transaction đầu tiên giữ lock; các transactions đến sau phải chờ và sẽ bị báo lỗi khi ghế đã chuyển sang `HELD`.

### B. Trùng vé do lỗi mạng (Duplicate Bookings)
- **Giải pháp:** `Idempotency-Key` (UUID) trong HTTP Header. Bảng `bookings` có `UNIQUE` constraint trên cột này.

### C. Gian lận Voucher
- **Giải pháp:** Atomic updates với Pessimistic Write Lock (`findByCodeForUpdate`). Kiểm tra `current_usage < max_usage`, cộng lên 1, và lưu trong cùng transaction boundary. Đã chứng minh bằng `VoucherConcurrencyTest.java`.

### D. Quá tải Flash Sale (50.000+ users)
- **Giải pháp:** Caffeine In-Memory Cache với TTL 2 giây cho endpoint `/seats`.
  - *Trade-off:* Users có thể thấy data trễ tối đa 2 giây.
  - *Lợi ích:* Database chỉ bị query 1 lần / 2 giây. 99.99% requests được phục vụ từ RAM.
  - *An toàn:* Nếu user click vào ghế đã bị mua (stale data), Pessimistic Lock ở luồng Write sẽ chặn lại.

## 5. Concurrency Tests (Bằng chứng thực nghiệm)

Dự án bao gồm các Integration Tests chạy trực tiếp trên Database thật (không mock):

- **`BookingServiceConcurrencyTest`:** Giả lập 10 threads cùng đặt 1 ghế. Kết quả: chỉ đúng 1 booking thành công, 9 bị reject.
- **`VoucherConcurrencyTest`:** Giả lập 10 threads cùng apply 1 voucher (max_usage = 5). Kết quả: chỉ đúng 5 lượt dùng thành công.
- **`BookingServiceConcurrencyTest` (Idempotency):** Gửi 2 requests cùng idempotency key. Kết quả: request thứ 2 bị reject với `IllegalStateException`.

Chạy toàn bộ tests:
```bash
./mvnw test
```