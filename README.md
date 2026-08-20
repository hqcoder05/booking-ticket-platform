# Concert Ticket Booking Platform

Backend cho nÃ¡Â»Ân tÃ¡ÂºÂ£ng Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© concert online Ã¢â‚¬â€ bÃƒÂ i test **Product Backend Intern, GEEK Up Geek Internship Autumn 2026**.

HÃ¡Â»â€¡ thÃ¡Â»â€˜ng gÃ¡Â»â€œm 2 luÃ¡Â»â€œng chÃƒÂ­nh:
- **Customer-facing**: browse concert, xem loÃ¡ÂºÂ¡i vÃƒÂ©, Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ©, ÃƒÂ¡p voucher, theo dÃƒÂµi trÃ¡ÂºÂ¡ng thÃƒÂ¡i booking
- **Operation Dashboard**: monitor booking, publish concert/vÃƒÂ©, quÃ¡ÂºÂ£n lÃƒÂ½ voucher (seed-only), xÃ¡Â»Â­ lÃƒÂ½ booking lÃ¡Â»â€”i, cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t trÃ¡ÂºÂ¡ng thÃƒÂ¡i thÃ¡Â»Â§ cÃƒÂ´ng

BÃƒÂ i toÃƒÂ¡n trÃ¡Â»Âng tÃƒÂ¢m: chÃ¡Â»â€˜ng **oversell vÃƒÂ©**, chÃ¡Â»â€˜ng **duplicate booking do retry**, chÃ¡Â»â€˜ng **lÃ¡ÂºÂ¡m dÃ¡Â»Â¥ng voucher**, vÃƒÂ  giÃ¡Â»Â¯ hÃ¡Â»â€¡ thÃ¡Â»â€˜ng **Ã¡Â»â€¢n Ã„â€˜Ã¡Â»â€¹nh khi traffic tÃ„Æ’ng Ã„â€˜Ã¡Â»â„¢t biÃ¡ÂºÂ¿n** (flash sale ~300Ã¢â‚¬â€œ500 booking request/phÃƒÂºt).

Ã°Å¸â€œâ€ž Chi tiÃ¡ÂºÂ¿t phÃƒÂ¢n tÃƒÂ­ch thiÃ¡ÂºÂ¿t kÃ¡ÂºÂ¿, ERD, cÃƒÂ¡c quyÃ¡ÂºÂ¿t Ã„â€˜Ã¡Â»â€¹nh kÃ¡Â»Â¹ thuÃ¡ÂºÂ­t vÃƒÂ  trade-off: xem [`system-design.md`](./system-design.md).

---

## Tech stack

| ThÃƒÂ nh phÃ¡ÂºÂ§n | CÃƒÂ´ng nghÃ¡Â»â€¡ |
|---|---|
| Backend | Spring Boot 4.1.0 + Java 21 |
| Database | PostgreSQL + Flyway (migration & seed data) |
| Auth | Spring Security + JWT (access token + refresh token) |
| API docs | Springdoc OpenAPI (Swagger UI) |
| Testing | JUnit 5 + Postman collection |
| Containerization | Docker Compose |

**KiÃ¡ÂºÂ¿n trÃƒÂºc:** Monolith, tÃƒÂ¡ch logic theo package `customer` / `operation`, mÃ¡Â»â€”i nhÃƒÂ³m cÃƒÂ³ prefix URL riÃƒÂªng vÃƒÂ  phÃƒÂ¢n quyÃ¡Â»Ân riÃƒÂªng theo `role` (`@PreAuthorize`). LÃƒÂ½ do lÃ¡Â»Â±a chÃ¡Â»Ân: traffic Ã„â€˜Ã¡Â»â€°nh thÃ¡Â»Â±c tÃ¡ÂºÂ¿ chÃ¡Â»â€° ~8Ã¢â‚¬â€œ9 request/giÃƒÂ¢y, khÃƒÂ´ng cÃ¡ÂºÂ§n microservices hay message queue Ã¢â‚¬â€ xem mÃ¡Â»Â¥c 2 trong `system-design.md`.

> **LÃ†Â°u ÃƒÂ½ phiÃƒÂªn bÃ¡ÂºÂ£n:** dÃ¡Â»Â± ÃƒÂ¡n dÃƒÂ¹ng Spring Boot 4.1.0 (yÃƒÂªu cÃ¡ÂºÂ§u tÃ¡Â»â€˜i thiÃ¡Â»Æ’u Java 17, baseline Jakarta EE 11 / Servlet 6.1). Java 21 Ã„â€˜ang dÃƒÂ¹ng thÃ¡Â»Âa mÃƒÂ£n yÃƒÂªu cÃ¡ÂºÂ§u nÃƒÂ y. NÃ¡ÂºÂ¿u mÃƒÂ´i trÃ†Â°Ã¡Â»Âng mÃƒÂ¡y bÃ¡ÂºÂ¡n Ã„â€˜ang chÃ¡ÂºÂ¡y JDK cÃ…Â© hÃ†Â¡n 17, cÃ¡ÂºÂ§n nÃƒÂ¢ng cÃ¡ÂºÂ¥p trÃ†Â°Ã¡Â»â€ºc khi build.

---

## CÃ¡ÂºÂ¥u trÃƒÂºc project

```
src/main/java/.../
Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ controller/
Ã¢â€â€š   Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ customer/        # API cho khÃƒÂ¡ch hÃƒÂ ng
Ã¢â€â€š   Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬ operation/        # API cho operator/admin
Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ service/
Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ repository/
Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ entity/
Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ dto/
Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ security/            # JWT, filter, role-based authorization
Ã¢â€Å“Ã¢â€â‚¬Ã¢â€â‚¬ config/
Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬ exception/

src/main/resources/
Ã¢â€â€Ã¢â€â‚¬Ã¢â€â‚¬ db/migration/        # Flyway migration scripts (schema + seed data)

src/test/java/...        # Unit tests (JUnit 5)

postman/                 # Postman collection cho API testing
```

---

## CÃƒÂ i Ã„â€˜Ã¡ÂºÂ·t & chÃ¡ÂºÂ¡y local

### YÃƒÂªu cÃ¡ÂºÂ§u
- Java 21
- Docker & Docker Compose
- Maven (hoÃ¡ÂºÂ·c dÃƒÂ¹ng `./mvnw` Ã„â€˜i kÃƒÂ¨m)

### CÃƒÂ¡c bÃ†Â°Ã¡Â»â€ºc

1. **Clone repo**
   ```bash
   git clone https://github.com/hqcoder05/booking-ticket-platform.git
   cd booking-ticket-platform
   ```

2. **Copy file cÃ¡ÂºÂ¥u hÃƒÂ¬nh mÃƒÂ´i trÃ†Â°Ã¡Â»Âng**
   ```bash
   cp .env.example .env
   ```

3. **KhÃ¡Â»Å¸i Ã„â€˜Ã¡Â»â„¢ng PostgreSQL bÃ¡ÂºÂ±ng Docker Compose**
   ```bash
   docker-compose up -d
   ```

4. **ChÃ¡ÂºÂ¡y Flyway migration (tÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng khi start app, hoÃ¡ÂºÂ·c chÃ¡ÂºÂ¡y tay)**
   ```bash
   ./mvnw flyway:migrate
   ```

5. **ChÃ¡ÂºÂ¡y Ã¡Â»Â©ng dÃ¡Â»Â¥ng**
   ```bash
   ./mvnw spring-boot:run
   ```

6. **KiÃ¡Â»Æ’m tra**
   - App: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`

### ChÃ¡ÂºÂ¡y unit test

```bash
./mvnw test
```

---

## API Documentation

- **Swagger UI**: `http://localhost:8080/swagger-ui.html` (sau khi chÃ¡ÂºÂ¡y app Ã¡Â»Å¸ local)
- **Postman collection**: xem thÃ†Â° mÃ¡Â»Â¥c [`postman/`](./postman) Ã¢â‚¬â€ import file `.json` vÃƒÂ o Postman, Ã„â€˜ÃƒÂ£ cÃ¡ÂºÂ¥u hÃƒÂ¬nh sÃ¡ÂºÂµn Ã„â€˜Ã¡Â»Æ’ chÃ¡ÂºÂ¡y vÃ¡Â»â€ºi local setup (base URL `http://localhost:8080`)

### NhÃƒÂ³m API chÃƒÂ­nh

**Customer-facing** (`/api/v1/customer`)
- Auth: register / login / refresh / logout
- Concert & vÃƒÂ©: xem danh sÃƒÂ¡ch, chi tiÃ¡ÂºÂ¿t, ghÃ¡ÂºÂ¿ trÃ¡Â»â€˜ng
- Booking: tÃ¡ÂºÂ¡o booking (kÃƒÂ¨m header `Idempotency-Key`), xem trÃ¡ÂºÂ¡ng thÃƒÂ¡i, thanh toÃƒÂ¡n (mock)
- Voucher: validate voucher trÃ†Â°Ã¡Â»â€ºc khi ÃƒÂ¡p dÃ¡Â»Â¥ng

**Operation** (`/api/v1/operation`, role `OPERATOR` / `ADMIN`)
- Venue, Concert, Ticket category: tÃ¡ÂºÂ¡o/publish/cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t
- Booking: xem danh sÃƒÂ¡ch (filter theo status/concert/date), cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t trÃ¡ÂºÂ¡ng thÃƒÂ¡i thÃ¡Â»Â§ cÃƒÂ´ng
- Voucher: chÃ¡Â»â€° xem (seed-only, khÃƒÂ´ng CRUD)

Danh sÃƒÂ¡ch endpoint Ã„â€˜Ã¡ÂºÂ§y Ã„â€˜Ã¡Â»Â§: xem mÃ¡Â»Â¥c 10 trong [`system-design.md`](./system-design.md).

---

## Coding guideline & convention

### ThÃƒÂªm mÃ¡Â»â„¢t API mÃ¡Â»â€ºi

1. XÃƒÂ¡c Ã„â€˜Ã¡Â»â€¹nh API thuÃ¡Â»â„¢c nhÃƒÂ³m `customer` hay `operation` Ã¢â€ â€™ tÃ¡ÂºÂ¡o/Ã„â€˜Ã¡ÂºÂ·t controller vÃƒÂ o Ã„â€˜ÃƒÂºng package tÃ†Â°Ã†Â¡ng Ã¡Â»Â©ng, Ã„â€˜Ã¡ÂºÂ·t prefix URL Ã„â€˜ÃƒÂºng chuÃ¡ÂºÂ©n (`/api/v1/customer/...` hoÃ¡ÂºÂ·c `/api/v1/operation/...`).
2. ViÃ¡ÂºÂ¿t DTO request/response riÃƒÂªng (khÃƒÂ´ng expose entity trÃ¡Â»Â±c tiÃ¡ÂºÂ¿p qua API).
3. ViÃ¡ÂºÂ¿t logic nghiÃ¡Â»â€¡p vÃ¡Â»Â¥ trong `service`, khÃƒÂ´ng viÃ¡ÂºÂ¿t trÃ¡Â»Â±c tiÃ¡ÂºÂ¿p trong controller.
4. NÃ¡ÂºÂ¿u thao tÃƒÂ¡c liÃƒÂªn quan Ã„â€˜Ã¡ÂºÂ¿n tÃƒÂ i nguyÃƒÂªn khan hiÃ¡ÂºÂ¿m (ghÃ¡ÂºÂ¿, sÃ¡Â»â€˜ lÃ†Â°Ã¡Â»Â£ng vÃƒÂ©, voucher) Ã¢â€ â€™ Ã„â€˜Ã¡ÂºÂ£m bÃ¡ÂºÂ£o nÃ¡ÂºÂ±m trong 1 transaction vÃƒÂ  ÃƒÂ¡p dÃ¡Â»Â¥ng Ã„â€˜ÃƒÂºng cÃ†Â¡ chÃ¡ÂºÂ¿ lock Ã„â€˜ÃƒÂ£ mÃƒÂ´ tÃ¡ÂºÂ£ trong `system-design.md` (mÃ¡Â»Â¥c 5, 6, 7).
5. GÃ¡ÂºÂ¯n `@PreAuthorize` theo `role` phÃƒÂ¹ hÃ¡Â»Â£p cho endpoint thuÃ¡Â»â„¢c `operation`.
6. CÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t Swagger annotation (`@Operation`, `@ApiResponse`) cho endpoint mÃ¡Â»â€ºi.
7. ViÃ¡ÂºÂ¿t unit test tÃ†Â°Ã†Â¡ng Ã¡Â»Â©ng trong `src/test/java`.
8. ThÃƒÂªm request mÃ¡ÂºÂ«u vÃƒÂ o Postman collection.

### ChÃ¡ÂºÂ¡y unit test

```bash
./mvnw test
```

Test chÃ¡ÂºÂ¡y trÃƒÂªn profile riÃƒÂªng (`test`), dÃƒÂ¹ng DB test tÃƒÂ¡ch biÃ¡Â»â€¡t Ã¢â‚¬â€ khÃƒÂ´ng Ã¡ÂºÂ£nh hÃ†Â°Ã¡Â»Å¸ng dÃ¡Â»Â¯ liÃ¡Â»â€¡u Ã¡Â»Å¸ `dev`.

---

## Assumptions & Scope

TÃƒÂ i liÃ¡Â»â€¡u Ã„â€˜Ã¡ÂºÂ§y Ã„â€˜Ã¡Â»Â§ vÃ¡Â»Â cÃƒÂ¡c giÃ¡ÂºÂ£ Ã„â€˜Ã¡Â»â€¹nh, giÃ¡Â»â€ºi hÃ¡ÂºÂ¡n vÃƒÂ  nhÃ¡Â»Â¯ng gÃƒÂ¬ Ã„â€˜ÃƒÂ£/chÃ†Â°a triÃ¡Â»Æ’n khai: xem mÃ¡Â»Â¥c 11 trong [`system-design.md`](./system-design.md).

**Ã„ÂÃƒÂ£ lÃƒÂ m (in scope):**
- KiÃ¡ÂºÂ¿n trÃƒÂºc monolith, tÃƒÂ¡ch package customer/operation
- ChÃ¡Â»â€˜ng oversell bÃ¡ÂºÂ±ng Pessimistic Lock (`SELECT ... FOR UPDATE`) cho cÃ¡ÂºÂ£ vÃƒÂ© VIP (SEATED) vÃƒÂ  Standard (STANDING)
- ChÃ¡Â»â€˜ng duplicate booking bÃ¡ÂºÂ±ng `Idempotency-Key` (unique constraint + exception handling)
- ChÃ¡Â»â€˜ng lÃ¡ÂºÂ¡m dÃ¡Â»Â¥ng voucher bÃ¡ÂºÂ±ng atomic UPDATE
- GiÃ¡Â»Â¯ ghÃ¡ÂºÂ¿ VIP 5 phÃƒÂºt, tÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng nhÃ¡ÂºÂ£ qua Scheduled Job
- State machine Ã„â€˜Ã¡ÂºÂ§y Ã„â€˜Ã¡Â»Â§ cho Booking vÃƒÂ  Seat
- Bulk refund khi operator hÃ¡Â»Â§y concert

**ChÃ†Â°a lÃƒÂ m (out of scope):**
- CRUD voucher tÃ¡Â»Â« operation dashboard Ã¢â‚¬â€ chÃ¡Â»â€° seed data qua Flyway
- TÃƒÂ­ch hÃ¡Â»Â£p cÃ¡Â»â€¢ng thanh toÃƒÂ¡n thÃ¡ÂºÂ­t Ã¢â‚¬â€ chÃ¡Â»â€° mock
- KhÃƒÂ¡ch hÃƒÂ ng tÃ¡Â»Â± hÃ¡Â»Â§y/hoÃƒÂ n tiÃ¡Â»Ân booking Ã„â€˜Ã†Â¡n lÃ¡ÂºÂ» (chÃƒÂ­nh sÃƒÂ¡ch no refund, no cancel)
- CÃ†Â¡ chÃ¡ÂºÂ¿ hÃ¡ÂºÂ¿t hÃ¡ÂºÂ¡n/dÃ¡Â»Ân dÃ¡ÂºÂ¹p tÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng cho idempotency key
- Redis TTL cho viÃ¡Â»â€¡c nhÃ¡ÂºÂ£ ghÃ¡ÂºÂ¿ (Ã„â€˜ÃƒÂ£ cÃƒÂ¢n nhÃ¡ÂºÂ¯c, chÃ¡Â»Ân Scheduled Job)
- Real-time push notification (WebSocket/SignalR)
- Rate limiting nÃƒÂ¢ng cao Ã¡Â»Å¸ tÃ¡ÂºÂ§ng API gateway

---

## TÃƒÂ¡c giÃ¡ÂºÂ£

**HoÃƒÂ ng NguyÃ¡Â»â€¦n ViÃ¡ÂºÂ¿t QuÃ¡Â»â€˜c** Ã¢â‚¬â€ Ã¡Â»Â©ng tuyÃ¡Â»Æ’n Product Backend Intern, GEEK Up Geek Internship Autumn 2026
