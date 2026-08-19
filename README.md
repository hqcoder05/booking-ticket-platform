# Concert Ticket Booking Platform

Backend cho nền tảng đặt vé concert online — bài test **Product Backend Intern, GEEK Up Geek Internship Autumn 2026**.

Hệ thống gồm 2 luồng chính:
- **Customer-facing**: browse concert, xem loại vé, đặt vé, áp voucher, theo dõi trạng thái booking
- **Operation Dashboard**: monitor booking, publish concert/vé, quản lý voucher (seed-only), xử lý booking lỗi, cập nhật trạng thái thủ công

Bài toán trọng tâm: chống **oversell vé**, chống **duplicate booking do retry**, chống **lạm dụng voucher**, và giữ hệ thống **ổn định khi traffic tăng đột biến** (flash sale ~300–500 booking request/phút).

📄 Chi tiết phân tích thiết kế, ERD, các quyết định kỹ thuật và trade-off: xem [`system-design.md`](./system-design.md).

---

## Tech stack

| Thành phần | Công nghệ |
|---|---|
| Backend | Spring Boot 4.1.0 + Java 21 |
| Database | PostgreSQL + Flyway (migration & seed data) |
| Auth | Spring Security + JWT (access token + refresh token) |
| API docs | Springdoc OpenAPI (Swagger UI) |
| Testing | JUnit 5 + Postman collection |
| Containerization | Docker Compose |

**Kiến trúc:** Monolith, tách logic theo package `customer` / `operation`, mỗi nhóm có prefix URL riêng và phân quyền riêng theo `role` (`@PreAuthorize`). Lý do lựa chọn: traffic đỉnh thực tế chỉ ~8–9 request/giây, không cần microservices hay message queue — xem mục 2 trong `system-design.md`.

> **Lưu ý phiên bản:** dự án dùng Spring Boot 4.1.0 (yêu cầu tối thiểu Java 17, baseline Jakarta EE 11 / Servlet 6.1). Java 21 đang dùng thỏa mãn yêu cầu này. Nếu môi trường máy bạn đang chạy JDK cũ hơn 17, cần nâng cấp trước khi build.

---

## Cấu trúc project

```
src/main/java/.../
├── controller/
│   ├── customer/        # API cho khách hàng
│   └── operation/        # API cho operator/admin
├── service/
├── repository/
├── entity/
├── dto/
├── security/            # JWT, filter, role-based authorization
├── config/
└── exception/

src/main/resources/
└── db/migration/        # Flyway migration scripts (schema + seed data)

src/test/java/...        # Unit tests (JUnit 5)

postman/                 # Postman collection cho API testing
```

---

## Cài đặt & chạy local

### Yêu cầu
- Java 21
- Docker & Docker Compose
- Maven (hoặc dùng `./mvnw` đi kèm)

### Các bước

1. **Clone repo**
   ```bash
   git clone https://github.com/hqcoder05/booking-ticket-platform.git
   cd booking-ticket-platform
   ```

2. **Copy file cấu hình môi trường**
   ```bash
   cp .env.example .env
   ```

3. **Khởi động PostgreSQL bằng Docker Compose**
   ```bash
   docker-compose up -d
   ```

4. **Chạy Flyway migration (tự động khi start app, hoặc chạy tay)**
   ```bash
   ./mvnw flyway:migrate
   ```

5. **Chạy ứng dụng**
   ```bash
   ./mvnw spring-boot:run
   ```

6. **Kiểm tra**
   - App: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`

### Chạy unit test

```bash
./mvnw test
```

---

## API Documentation

- **Swagger UI**: `http://localhost:8080/swagger-ui.html` (sau khi chạy app ở local)
- **Postman collection**: xem thư mục [`postman/`](./postman) — import file `.json` vào Postman, đã cấu hình sẵn để chạy với local setup (base URL `http://localhost:8080`)

### Nhóm API chính

**Customer-facing** (`/api/v1/customer`)
- Auth: register / login / refresh / logout
- Concert & vé: xem danh sách, chi tiết, ghế trống
- Booking: tạo booking (kèm header `Idempotency-Key`), xem trạng thái, thanh toán (mock)
- Voucher: validate voucher trước khi áp dụng

**Operation** (`/api/v1/operation`, role `OPERATOR` / `ADMIN`)
- Venue, Concert, Ticket category: tạo/publish/cập nhật
- Booking: xem danh sách (filter theo status/concert/date), cập nhật trạng thái thủ công
- Voucher: chỉ xem (seed-only, không CRUD)

Danh sách endpoint đầy đủ: xem mục 10 trong [`system-design.md`](./system-design.md).

---

## Coding guideline & convention

### Thêm một API mới

1. Xác định API thuộc nhóm `customer` hay `operation` → tạo/đặt controller vào đúng package tương ứng, đặt prefix URL đúng chuẩn (`/api/v1/customer/...` hoặc `/api/v1/operation/...`).
2. Viết DTO request/response riêng (không expose entity trực tiếp qua API).
3. Viết logic nghiệp vụ trong `service`, không viết trực tiếp trong controller.
4. Nếu thao tác liên quan đến tài nguyên khan hiếm (ghế, số lượng vé, voucher) → đảm bảo nằm trong 1 transaction và áp dụng đúng cơ chế lock đã mô tả trong `system-design.md` (mục 5, 6, 7).
5. Gắn `@PreAuthorize` theo `role` phù hợp cho endpoint thuộc `operation`.
6. Cập nhật Swagger annotation (`@Operation`, `@ApiResponse`) cho endpoint mới.
7. Viết unit test tương ứng trong `src/test/java`.
8. Thêm request mẫu vào Postman collection.

### Chạy unit test

```bash
./mvnw test
```

Test chạy trên profile riêng (`test`), dùng DB test tách biệt — không ảnh hưởng dữ liệu ở `dev`.

---

## Assumptions & Scope

Tài liệu đầy đủ về các giả định, giới hạn và những gì đã/chưa triển khai: xem mục 11 trong [`system-design.md`](./system-design.md).

**Đã làm (in scope):**
- Kiến trúc monolith, tách package customer/operation
- Chống oversell bằng Pessimistic Lock (`SELECT ... FOR UPDATE`) cho cả vé VIP (SEATED) và Standard (STANDING)
- Chống duplicate booking bằng `Idempotency-Key` (unique constraint + exception handling)
- Chống lạm dụng voucher bằng atomic UPDATE
- Giữ ghế VIP 5 phút, tự động nhả qua Scheduled Job
- State machine đầy đủ cho Booking và Seat
- Bulk refund khi operator hủy concert

**Chưa làm (out of scope):**
- CRUD voucher từ operation dashboard — chỉ seed data qua Flyway
- Tích hợp cổng thanh toán thật — chỉ mock
- Khách hàng tự hủy/hoàn tiền booking đơn lẻ (chính sách no refund, no cancel)
- Cơ chế hết hạn/dọn dẹp tự động cho idempotency key
- Redis TTL cho việc nhả ghế (đã cân nhắc, chọn Scheduled Job)
- Real-time push notification (WebSocket/SignalR)
- Rate limiting nâng cao ở tầng API gateway

---

## Tác giả

**Hoàng Nguyễn Viết Quốc** — ứng tuyển Product Backend Intern, GEEK Up Geek Internship Autumn 2026
