# Concert Ticket Booking Platform — System & Database Design

**Tác giả:** Hoàng Nguyễn Viết Quốc
**Vị trí ứng tuyển:** Product Backend Intern — GEEK Up Geek Internship Autumn 2026

---

## 1. Business context tóm tắt

Nền tảng đặt vé concert online, gồm 2 luồng:
- **Customer-facing**: browse concert, xem loại vé, đặt vé, áp voucher, theo dõi trạng thái booking
- **Operation Dashboard**: monitor booking, publish concert/vé, quản lý voucher (seed-only), xử lý booking lỗi, cập nhật trạng thái thủ công

**Ràng buộc kỹ thuật quan trọng:**
- Traffic đỉnh: ~50,000 users, 300–500 booking request/phút trong flash sale
- Phải chống: oversell vé, duplicate booking do retry, lạm dụng voucher, sập hệ thống khi traffic tăng đột biến

---

## 2. Kiến trúc tổng quan

**Lựa chọn: Monolith, tách logic theo package (customer / operation)**

Lý do:
- Traffic đỉnh thực tế chỉ ~8–9 request/giây (300–500/phút) — không cần microservices hay message queue phức tạp
- Monolith giảm độ phức tạp vận hành, phù hợp với thời gian phát triển giới hạn của bài test
- Vẫn tách rõ ràng ở tầng code: `controller/customer/...` và `controller/operation/...`, mỗi nhóm có prefix URL riêng và authorization riêng theo `role` (Spring Security `@PreAuthorize`)

**Tech stack:**
- Backend: Spring Boot 3 + Java 21
- Database: PostgreSQL + Flyway (migration & seed data)
- Containerization: Docker Compose
- Auth: Spring Security + JWT (access token + refresh token)
- API docs: Springdoc OpenAPI (Swagger UI)
- Testing: JUnit 5 + Postman collection

Lý do chọn stack: đã có kinh nghiệm triển khai thực tế qua các dự án trước (ShiftSync, LinkUp), giảm thời gian setup, tập trung thời gian vào phần logic nghiệp vụ cốt lõi.

---

## 3. Entity Relationship Diagram (ERD)

```
erDiagram
  USERS ||--o{ BOOKINGS : places
  USERS ||--o{ REFRESH_TOKENS : owns
  USERS ||--o{ NOTIFICATIONS : receives
  VENUES ||--o{ CONCERTS : hosts
  CONCERTS ||--o{ TICKET_CATEGORIES : has
  CONCERTS ||--o{ BOOKINGS : "booked for"
  TICKET_CATEGORIES ||--o{ SEATS : "contains (VIP)"
  SEATS ||--o| BOOKING_ITEMS : "held by"
  TICKET_CATEGORIES ||--o{ BOOKING_ITEMS : "sold as (Standard)"
  BOOKINGS ||--o{ BOOKING_ITEMS : contains
  VOUCHERS ||--o{ BOOKINGS : "applied to"
  BOOKINGS ||--o| PAYMENTS : "paid via"
  BOOKINGS ||--o{ EMAIL_LOGS : triggers

  USERS {
    uuid id PK
    string email
    string password_hash
    string role
  }
  REFRESH_TOKENS {
    uuid id PK
    uuid user_id FK
    string token_hash
    datetime expires_at
    boolean revoked
  }
  VENUES {
    uuid id PK
    string name
    string address
    string city
    int capacity
  }
  CONCERTS {
    uuid id PK
    uuid venue_id FK
    string name
    datetime event_date
    string status
  }
  TICKET_CATEGORIES {
    uuid id PK
    uuid concert_id FK
    string name
    string type
    decimal price
    int total_quantity
    int available_quantity
    int version
  }
  SEATS {
    uuid id PK
    uuid ticket_category_id FK
    string seat_number
    string status
    datetime held_until
    uuid held_by_booking_id FK
    int version
  }
  VOUCHERS {
    uuid id PK
    string code
    string discount_type
    decimal discount_value
    int max_usage
    int current_usage
  }
  BOOKINGS {
    uuid id PK
    uuid user_id FK
    uuid concert_id FK
    uuid voucher_id FK
    string idempotency_key
    string status
    decimal total_amount
  }
  BOOKING_ITEMS {
    uuid id PK
    uuid booking_id FK
    uuid ticket_category_id FK
    uuid seat_id FK
    int quantity
    decimal unit_price
  }
  PAYMENTS {
    uuid id PK
    uuid booking_id FK
    string status
    string method
  }
  NOTIFICATIONS {
    uuid id PK
    uuid user_id FK
    string type
    string message
    string status
    datetime sent_at
  }
  EMAIL_LOGS {
    uuid id PK
    uuid booking_id FK
    string to_address
    string subject
    string status
    datetime sent_at
  }
```

### Giải thích vai trò từng bảng

| Bảng | Vai trò |
|---|---|
| USERS | Tài khoản, phân biệt CUSTOMER / OPERATOR / ADMIN qua `role` |
| REFRESH_TOKENS | Cấp lại access token JWT mà không bắt đăng nhập lại; hỗ trợ logout/thu hồi |
| VENUES | Địa điểm tổ chức, tái sử dụng cho nhiều concert |
| CONCERTS | Sự kiện concert, `status`: DRAFT / PUBLISHED / CLOSED / CANCELLED |
| TICKET_CATEGORIES | Loại vé (VIP/Standard), `type`: SEATED / STANDING — trung tâm của cơ chế chống oversell |
| SEATS | Từng ghế VIP cụ thể, chỉ áp dụng cho category SEATED |
| VOUCHERS | Mã giảm giá, giới hạn qua `max_usage` / `current_usage` |
| BOOKINGS | Đơn đặt vé, trung tâm nối User–Concert–Voucher, chống duplicate qua `idempotency_key` |
| BOOKING_ITEMS | Chi tiết từng vé trong 1 booking (seat_id cho VIP, quantity cho Standard) |
| PAYMENTS | Trạng thái thanh toán, tách riêng để dễ đánh dấu là mock |
| NOTIFICATIONS | Thông báo trong app |
| EMAIL_LOGS | Log gửi email xác nhận/hoàn tiền |

---

## 4. Phân loại vé: VIP (SEATED) vs Standard (STANDING)

| | VIP — SEATED | Standard — STANDING |
|---|---|---|
| Khi đặt | Chọn 1 ghế cụ thể (seat_id) | Chỉ chọn số lượng (quantity) |
| Chống oversell | Lock từng dòng SEATS | Atomic decrement `available_quantity` |
| BOOKING_ITEMS | `seat_id` có giá trị, quantity = 1 | `seat_id` = null, quantity > 1 |

Nhánh rẽ trong service layer:
```java
if (ticketCategory.getType() == TicketType.SEATED) {
    // Lock ghế cụ thể qua seat_id, kiểm tra status = AVAILABLE
} else { // STANDING
    // UPDATE ticket_categories SET available_quantity = available_quantity - :qty
    // WHERE id = :id AND available_quantity >= :qty
}
```

**Assumption:** mỗi concert seed tối đa 2 loại vé (VIP, Standard). Kiến trúc hỗ trợ mở rộng thêm category khác nhờ field `type`, nhưng phạm vi bài test chỉ dùng 2 loại này.

---

## 5. Cơ chế chống Oversell — Pessimistic Lock

**Lựa chọn:** `SELECT ... FOR UPDATE` (Pessimistic Lock), không dùng Optimistic Lock (version-based).

### So sánh và lý do quyết định

| Tiêu chí | Pessimistic Lock | Optimistic Lock |
|---|---|---|
| Hiệu năng khi ít conflict | Chậm hơn | Nhanh hơn |
| Hiệu năng khi nhiều conflict (flash sale) | Ổn định, dễ đoán | Retry storm, tăng tải đúng lúc cần ổn định nhất |
| Độ phức tạp code | Đơn giản, transaction tự lo | Phức tạp hơn, phải tự viết retry logic |
| Độ chắc chắn | Thành công/thất bại rõ ràng ngay lần đầu | Có thể phải thử lại nhiều lần |

**Lý do chọn Pessimistic Lock:**
1. Bài toán có tính chất "tài nguyên khan hiếm, tranh chấp cao" đúng lúc flash sale — kịch bản mà Optimistic Lock hoạt động kém nhất do tỷ lệ conflict cao.
2. Traffic thực tế (~8-9 req/giây) hoàn toàn nằm trong khả năng xử lý tốt của Pessimistic Lock, chưa đến mức cần cân nhắc giải pháp khác.
3. Đảm bảo tính đúng đắn tuyệt đối, dễ chứng minh và test — quan trọng khi thời gian phát triển giới hạn.

### Áp dụng cụ thể

**VIP (SEATED):**
```sql
SELECT * FROM seats WHERE id = :seatId AND status = 'AVAILABLE' FOR UPDATE;
-- nếu lấy được row → UPDATE status = 'HELD', held_until = now() + 5 phút
-- nếu không lấy được (đang bị lock) → báo "ghế đã được người khác giữ"
```

**Standard (STANDING):**
```sql
SELECT * FROM ticket_categories WHERE id = :categoryId FOR UPDATE;
-- kiểm tra available_quantity >= quantity trong cùng transaction rồi trừ
```

---

## 6. Cơ chế chống Duplicate Booking — Idempotency Key

**Vấn đề:** khách bấm "Đặt vé" nhưng timeout/mạng chậm, bấm lại → tạo 2 booking trùng cho cùng 1 yêu cầu.

**Thiết kế:**

1. Client tự sinh UUID **trước khi gửi request**, gửi qua header `Idempotency-Key`. Nếu bấm lại do timeout, gửi lại **cùng key đó**.
2. Ràng buộc unique ở tầng database:
```sql
ALTER TABLE bookings ADD CONSTRAINT uq_idempotency_key UNIQUE (idempotency_key);
```
3. Xử lý race condition ở tầng code — không chỉ SELECT-rồi-INSERT, mà bắt lỗi unique violation:
```java
try {
    Booking booking = bookingRepository.save(newBooking);
} catch (DataIntegrityViolationException e) {
    Booking existing = bookingRepository.findByIdempotencyKey(key);
    return existing; // trả về booking đã tồn tại, không tạo trùng
}
```

**Assumption:** idempotency key hiện không có cơ chế hết hạn/dọn dẹp tự động. Nếu triển khai production cần thêm scheduled job xóa key cũ hơn 24–48h.

---

## 7. Cơ chế chống lạm dụng Voucher — Atomic Update

**Vấn đề (Lost Update):** nhiều request cùng đọc `current_usage`, cùng cộng dồn và ghi đè lên nhau → số lượt dùng thực tế bị "nuốt mất", vượt quá `max_usage` mà hệ thống không phát hiện.

**Giải pháp:** gộp đọc và ghi thành 1 câu lệnh atomic tại database, không tách thành 2 bước ở tầng application:
```sql
UPDATE vouchers
SET current_usage = current_usage + 1
WHERE id = :voucherId
  AND current_usage < max_usage;
```

```java
@Modifying
@Query("UPDATE Voucher v SET v.currentUsage = v.currentUsage + 1 " +
       "WHERE v.id = :id AND v.currentUsage < v.maxUsage")
int applyVoucher(@Param("id") UUID voucherId);
```
```java
int rowsAffected = voucherRepository.applyVoucher(voucherId);
if (rowsAffected == 0) {
    throw new VoucherLimitExceededException("Voucher đã hết lượt sử dụng");
}
```

`rowsAffected == 0` nghĩa là điều kiện sai tại thời điểm ghi (đã hết slot) → từ chối ngay. Voucher được apply trong cùng transaction với bước lock ghế/trừ vé — nếu bước nào fail thì rollback toàn bộ.

---

## 8. State Machine

### 8.1 Booking

```
Received → Pending payment → Completed         (thanh toán thành công)
                            → Expired            (hết 5 phút chưa thanh toán)
                            → Failed              (thanh toán lỗi)
Completed → Refunded                              (chỉ khi operator hủy toàn bộ concert — bulk refund)
```

**Assumption quan trọng:** hệ thống không hỗ trợ khách hàng tự hủy/yêu cầu hoàn tiền booking đơn lẻ (chính sách "no refund, no cancel" đúng thực tế ngành ticketing). `Refunded` chỉ xảy ra khi operator hủy toàn bộ concert, áp dụng hàng loạt cho các booking đã `Completed`.

### 8.2 Seat (chỉ áp dụng cho vé VIP — SEATED)

```
Available → Held (giữ 5 phút, khi khách chọn ghế)
Held → Booked (khi thanh toán xong)
Held → Available (tự động, khi hết 5 phút — do Scheduled Job xử lý)
```

### 8.3 Mối liên kết giữa 2 state machine

| Booking chuyển | Seat/Ticket category tương ứng |
|---|---|
| Received → Pending payment | Seat: Available → Held (VIP) / trừ available_quantity (Standard) |
| → Completed | Seat: Held → Booked |
| → Expired / Failed | Seat: Held → Available (nhả lại) / cộng trả available_quantity |

### 8.4 Cơ chế tự động nhả ghế sau 5 phút

**Chọn: Scheduled Job** (thay vì Redis TTL + keyspace notification).

Lý do: đơn giản hơn để implement và giải thích trong thời gian giới hạn, đủ chính xác cho scope bài test (độ trễ tối đa ~1 phút không phải vấn đề lớn ở quy mô traffic này). Đã cân nhắc Redis TTL (có kinh nghiệm từ dự án ShiftSync) nhưng chọn Scheduled Job vì đơn giản và đủ đáp ứng scale bài toán.

```java
@Scheduled(fixedRate = 60000) // chạy mỗi 1 phút
public void releaseExpiredHolds() {
    // Quét SEATS có status = HELD và held_until < now()
    // → set status = AVAILABLE, held_by_booking_id = null
    // → set booking liên quan sang status = EXPIRED
}
```

---

## 9. Luồng xử lý chính

### 9.1 Customer — đặt vé
```
Xem concert → Chọn loại vé
  → VIP: chọn ghế cụ thể
  → Standard: chọn số lượng
→ Tạo booking (idempotency key, lock ghế/vé trong transaction)
→ Áp voucher (tùy chọn, atomic update)
→ Thanh toán trong 5 phút
```

### 9.2 Operation — vận hành
```
Publish concert / tạo loại vé
Xem danh sách booking, filter theo trạng thái
Update trạng thái thủ công (xác nhận thanh toán thủ công / đánh dấu nghi vấn gian lận)
Hủy concert → bulk refund tự động cho các booking Completed
```

---

## 10. Danh sách API endpoints

### Customer-facing (`/api/v1/customer`)

**Auth**
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`

**Concert & vé**
- `GET /concerts`
- `GET /concerts/{id}`
- `GET /concerts/{id}/seats`

**Booking**
- `POST /bookings` (header `Idempotency-Key`)
- `GET /bookings/{id}`
- `GET /bookings`
- `POST /bookings/{id}/payment` (mock)

**Voucher**
- `POST /vouchers/validate`

### Operation (`/api/v1/operation`, role OPERATOR/ADMIN)

**Venue**
- `POST /venues`
- `GET /venues`

**Concert**
- `POST /concerts`
- `PUT /concerts/{id}`
- `PUT /concerts/{id}/publish`
- `PUT /concerts/{id}/cancel` (trigger bulk refund)
- `GET /concerts`

**Ticket category**
- `POST /concerts/{id}/ticket-categories`
- `GET /concerts/{id}/ticket-categories`

**Booking**
- `GET /bookings` (filter theo status/concert_id/date)
- `GET /bookings/{id}`
- `PUT /bookings/{id}/status`

**Voucher**
- `GET /vouchers` (chỉ xem, không CRUD — seed-only)

---

## 11. Tóm tắt Assumptions / Scope

**Đã làm (in scope):**
- Kiến trúc monolith, tách logic theo package customer/operation
- Chống oversell bằng Pessimistic Lock cho cả 2 loại vé (SEATED/STANDING)
- Chống duplicate booking bằng idempotency key (unique constraint + exception handling)
- Chống lạm dụng voucher bằng atomic UPDATE
- Cơ chế giữ ghế VIP 5 phút, tự động nhả qua Scheduled Job
- State machine đầy đủ cho Booking (5 trạng thái + Refunded) và Seat (3 trạng thái)
- Bulk refund khi operator hủy concert

**Chưa làm (out of scope, có thể mở rộng sau):**
- CRUD voucher từ operation dashboard — chỉ seed data qua Flyway migration
- Tích hợp cổng thanh toán thật (VNPay, Momo...) — chỉ mock
- Khách hàng tự hủy/yêu cầu hoàn tiền booking đơn lẻ — chính sách no refund, no cancel
- Cơ chế hết hạn/dọn dẹp tự động cho idempotency key
- Redis TTL cho việc nhả ghế (đã cân nhắc, chọn Scheduled Job vì đơn giản hơn và đủ đáp ứng scale)
- Real-time push notification — Notification chỉ lưu record, không có WebSocket/SignalR
- Rate limiting nâng cao ở tầng API gateway
