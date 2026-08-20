# Concert Ticket Booking Platform

Backend cho ná»n táº£ng Ä‘áº·t vÃ© concert online â€” bÃ i test **Product Backend Intern, GEEK Up Geek Internship Autumn 2026**.

Há»‡ thá»‘ng gá»“m 2 luá»“ng chÃ­nh:
- **Customer-facing**: browse concert, xem loáº¡i vÃ©, Ä‘áº·t vÃ©, Ã¡p voucher, theo dÃµi tráº¡ng thÃ¡i booking
- **Operation Dashboard**: monitor booking, publish concert/vÃ©, quáº£n lÃ½ voucher (seed-only), xá»­ lÃ½ booking lá»—i, cáº­p nháº­t tráº¡ng thÃ¡i thá»§ cÃ´ng

BÃ i toÃ¡n trá»ng tÃ¢m: chá»‘ng **oversell vÃ©**, chá»‘ng **duplicate booking do retry**, chá»‘ng **láº¡m dá»¥ng voucher**, vÃ  giá»¯ há»‡ thá»‘ng **á»•n Ä‘á»‹nh khi traffic tÄƒng Ä‘á»™t biáº¿n** (flash sale ~300â€“500 booking request/phÃºt).

ðŸ“„ Chi tiáº¿t phÃ¢n tÃ­ch thiáº¿t káº¿, ERD, cÃ¡c quyáº¿t Ä‘á»‹nh ká»¹ thuáº­t vÃ  trade-off: xem [`system-design.md`](./system-design.md).

---

## Tech stack

| ThÃ nh pháº§n | CÃ´ng nghá»‡ |
|---|---|
| Backend | Spring Boot 4.1.0 + Java 21 |
| Database | PostgreSQL + Flyway (migration & seed data) |
| Auth | Spring Security + JWT (access token + refresh token) |
| API docs | Springdoc OpenAPI (Swagger UI) |
| Testing | JUnit 5 + Postman collection |
| Containerization | Docker Compose |

**Kiáº¿n trÃºc:** Monolith, tÃ¡ch logic theo package `customer` / `operation`, má»—i nhÃ³m cÃ³ prefix URL riÃªng vÃ  phÃ¢n quyá»n riÃªng theo `role` (`@PreAuthorize`). LÃ½ do lá»±a chá»n: traffic Ä‘á»‰nh thá»±c táº¿ chá»‰ ~8â€“9 request/giÃ¢y, khÃ´ng cáº§n microservices hay message queue â€” xem má»¥c 2 trong `system-design.md`.

> **LÆ°u Ã½ phiÃªn báº£n:** dá»± Ã¡n dÃ¹ng Spring Boot 4.1.0 (yÃªu cáº§u tá»‘i thiá»ƒu Java 17, baseline Jakarta EE 11 / Servlet 6.1). Java 21 Ä‘ang dÃ¹ng thá»a mÃ£n yÃªu cáº§u nÃ y. Náº¿u mÃ´i trÆ°á»ng mÃ¡y báº¡n Ä‘ang cháº¡y JDK cÅ© hÆ¡n 17, cáº§n nÃ¢ng cáº¥p trÆ°á»›c khi build.

---

## Cáº¥u trÃºc project

```
src/main/java/.../
â”œâ”€â”€ controller/
â”‚   â”œâ”€â”€ customer/        # API cho khÃ¡ch hÃ ng
â”‚   â””â”€â”€ operation/        # API cho operator/admin
â”œâ”€â”€ service/
â”œâ”€â”€ repository/
â”œâ”€â”€ entity/
â”œâ”€â”€ dto/
â”œâ”€â”€ security/            # JWT, filter, role-based authorization
â”œâ”€â”€ config/
â””â”€â”€ exception/

src/main/resources/
â””â”€â”€ db/migration/        # Flyway migration scripts (schema + seed data)

src/test/java/...        # Unit tests (JUnit 5)

postman/                 # Postman collection cho API testing
```

---

## CÃ i Ä‘áº·t & cháº¡y local

### YÃªu cáº§u
- Java 21
- Docker & Docker Compose
- Maven (hoáº·c dÃ¹ng `./mvnw` Ä‘i kÃ¨m)

### CÃ¡c bÆ°á»›c

1. **Clone repo**
   ```bash
   git clone https://github.com/hqcoder05/booking-ticket-platform.git
   cd booking-ticket-platform
   ```

2. **Copy file cáº¥u hÃ¬nh mÃ´i trÆ°á»ng**
   ```bash
   cp .env.example .env
   ```

3. **Khá»Ÿi Ä‘á»™ng PostgreSQL báº±ng Docker Compose**
   ```bash
   docker-compose up -d
   ```

4. **Cháº¡y Flyway migration (tá»± Ä‘á»™ng khi start app, hoáº·c cháº¡y tay)**
   ```bash
   ./mvnw flyway:migrate
   ```

5. **Cháº¡y á»©ng dá»¥ng**
   ```bash
   ./mvnw spring-boot:run
   ```

6. **Kiá»ƒm tra**
   - App: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`

### Cháº¡y unit test

```bash
./mvnw test
```

---

## API Documentation

- **Swagger UI**: `http://localhost:8080/swagger-ui.html` (sau khi cháº¡y app á»Ÿ local)
- **Postman collection**: xem thÆ° má»¥c [`postman/`](./postman) â€” import file `.json` vÃ o Postman, Ä‘Ã£ cáº¥u hÃ¬nh sáºµn Ä‘á»ƒ cháº¡y vá»›i local setup (base URL `http://localhost:8080`)

### NhÃ³m API chÃ­nh

**Customer-facing** (`/api/v1/customer`)
- Auth: register / login / refresh / logout
- Concert & vÃ©: xem danh sÃ¡ch, chi tiáº¿t, gháº¿ trá»‘ng
- Booking: táº¡o booking (kÃ¨m header `Idempotency-Key`), xem tráº¡ng thÃ¡i, thanh toÃ¡n (mock)
- Voucher: validate voucher trÆ°á»›c khi Ã¡p dá»¥ng

**Operation** (`/api/v1/operation`, role `OPERATOR` / `ADMIN`)
- Venue, Concert, Ticket category: táº¡o/publish/cáº­p nháº­t
- Booking: xem danh sÃ¡ch (filter theo status/concert/date), cáº­p nháº­t tráº¡ng thÃ¡i thá»§ cÃ´ng
- Voucher: chá»‰ xem (seed-only, khÃ´ng CRUD)

Danh sÃ¡ch endpoint Ä‘áº§y Ä‘á»§: xem má»¥c 10 trong [`system-design.md`](./system-design.md).

---

## Coding guideline & convention

### ThÃªm má»™t API má»›i

1. XÃ¡c Ä‘á»‹nh API thuá»™c nhÃ³m `customer` hay `operation` â†’ táº¡o/Ä‘áº·t controller vÃ o Ä‘Ãºng package tÆ°Æ¡ng á»©ng, Ä‘áº·t prefix URL Ä‘Ãºng chuáº©n (`/api/v1/customer/...` hoáº·c `/api/v1/operation/...`).
2. Viáº¿t DTO request/response riÃªng (khÃ´ng expose entity trá»±c tiáº¿p qua API).
3. Viáº¿t logic nghiá»‡p vá»¥ trong `service`, khÃ´ng viáº¿t trá»±c tiáº¿p trong controller.
4. Náº¿u thao tÃ¡c liÃªn quan Ä‘áº¿n tÃ i nguyÃªn khan hiáº¿m (gháº¿, sá»‘ lÆ°á»£ng vÃ©, voucher) â†’ Ä‘áº£m báº£o náº±m trong 1 transaction vÃ  Ã¡p dá»¥ng Ä‘Ãºng cÆ¡ cháº¿ lock Ä‘Ã£ mÃ´ táº£ trong `system-design.md` (má»¥c 5, 6, 7).
5. Gáº¯n `@PreAuthorize` theo `role` phÃ¹ há»£p cho endpoint thuá»™c `operation`.
6. Cáº­p nháº­t Swagger annotation (`@Operation`, `@ApiResponse`) cho endpoint má»›i.
7. Viáº¿t unit test tÆ°Æ¡ng á»©ng trong `src/test/java`.
8. ThÃªm request máº«u vÃ o Postman collection.

### Cháº¡y unit test

```bash
./mvnw test
```

Test cháº¡y trÃªn profile riÃªng (`test`), dÃ¹ng DB test tÃ¡ch biá»‡t â€” khÃ´ng áº£nh hÆ°á»Ÿng dá»¯ liá»‡u á»Ÿ `dev`.

---

## Assumptions & Scope

TÃ i liá»‡u Ä‘áº§y Ä‘á»§ vá» cÃ¡c giáº£ Ä‘á»‹nh, giá»›i háº¡n vÃ  nhá»¯ng gÃ¬ Ä‘Ã£/chÆ°a triá»ƒn khai: xem má»¥c 11 trong [`system-design.md`](./system-design.md).

**ÄÃ£ lÃ m (in scope):**
- Kiáº¿n trÃºc monolith, tÃ¡ch package customer/operation
- Chá»‘ng oversell báº±ng Pessimistic Lock (`SELECT ... FOR UPDATE`) cho cáº£ vÃ© VIP (SEATED) vÃ  Standard (STANDING)
- Chá»‘ng duplicate booking báº±ng `Idempotency-Key` (unique constraint + exception handling)
- Chá»‘ng láº¡m dá»¥ng voucher báº±ng atomic UPDATE
- Giá»¯ gháº¿ VIP 5 phÃºt, tá»± Ä‘á»™ng nháº£ qua Scheduled Job
- State machine Ä‘áº§y Ä‘á»§ cho Booking vÃ  Seat
- Bulk refund khi operator há»§y concert

**ChÆ°a lÃ m (out of scope):**
- CRUD voucher tá»« operation dashboard â€” chá»‰ seed data qua Flyway
- TÃ­ch há»£p cá»•ng thanh toÃ¡n tháº­t â€” chá»‰ mock
- KhÃ¡ch hÃ ng tá»± há»§y/hoÃ n tiá»n booking Ä‘Æ¡n láº» (chÃ­nh sÃ¡ch no refund, no cancel)
- CÆ¡ cháº¿ háº¿t háº¡n/dá»n dáº¹p tá»± Ä‘á»™ng cho idempotency key
- Redis TTL cho viá»‡c nháº£ gháº¿ (Ä‘Ã£ cÃ¢n nháº¯c, chá»n Scheduled Job)
- Real-time push notification (WebSocket/SignalR)
- Rate limiting nÃ¢ng cao á»Ÿ táº§ng API gateway

---

## TÃ¡c giáº£

**HoÃ ng Nguyá»…n Viáº¿t Quá»‘c** â€” á»©ng tuyá»ƒn Product Backend Intern, GEEK Up Geek Internship Autumn 2026
