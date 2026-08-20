# Concert Ticket Booking Platform â€” System & Database Design

**TÃ¡c giáº£:** HoÃ ng Nguyá»…n Viáº¿t Quá»‘c
**Vá»‹ trÃ­ á»©ng tuyá»ƒn:** Product Backend Intern â€” GEEK Up Geek Internship Autumn 2026

---

## 1. Business context tÃ³m táº¯t

Ná»n táº£ng Ä‘áº·t vÃ© concert online, gá»“m 2 luá»“ng:
- **Customer-facing**: browse concert, xem loáº¡i vÃ©, Ä‘áº·t vÃ©, Ã¡p voucher, theo dÃµi tráº¡ng thÃ¡i booking
- **Operation Dashboard**: monitor booking, publish concert/vÃ©, quáº£n lÃ½ voucher (seed-only), xá»­ lÃ½ booking lá»—i, cáº­p nháº­t tráº¡ng thÃ¡i thá»§ cÃ´ng

**RÃ ng buá»™c ká»¹ thuáº­t quan trá»ng:**
- Traffic Ä‘á»‰nh: ~50,000 users, 300â€“500 booking request/phÃºt trong flash sale
- Pháº£i chá»‘ng: oversell vÃ©, duplicate booking do retry, láº¡m dá»¥ng voucher, sáº­p há»‡ thá»‘ng khi traffic tÄƒng Ä‘á»™t biáº¿n

---

## 2. Kiáº¿n trÃºc tá»•ng quan

**Lá»±a chá»n: Monolith, tÃ¡ch logic theo package (customer / operation)**

LÃ½ do:
- Traffic Ä‘á»‰nh thá»±c táº¿ chá»‰ ~8â€“9 request/giÃ¢y (300â€“500/phÃºt) â€” khÃ´ng cáº§n microservices hay message queue phá»©c táº¡p
- Monolith giáº£m Ä‘á»™ phá»©c táº¡p váº­n hÃ nh, phÃ¹ há»£p vá»›i thá»i gian phÃ¡t triá»ƒn giá»›i háº¡n cá»§a bÃ i test
- Váº«n tÃ¡ch rÃµ rÃ ng á»Ÿ táº§ng code: `controller/customer/...` vÃ  `controller/operation/...`, má»—i nhÃ³m cÃ³ prefix URL riÃªng vÃ  authorization riÃªng theo `role` (Spring Security `@PreAuthorize`)

**Tech stack:**
- Backend: Spring Boot 3 + Java 21
- Database: PostgreSQL + Flyway (migration & seed data)
- Containerization: Docker Compose
- Auth: Spring Security + JWT (access token + refresh token)
- API docs: Springdoc OpenAPI (Swagger UI)
- Testing: JUnit 5 + Postman collection

LÃ½ do chá»n stack: Ä‘Ã£ cÃ³ kinh nghiá»‡m triá»ƒn khai thá»±c táº¿ qua cÃ¡c dá»± Ã¡n trÆ°á»›c (ShiftSync, LinkUp), giáº£m thá»i gian setup, táº­p trung thá»i gian vÃ o pháº§n logic nghiá»‡p vá»¥ cá»‘t lÃµi.

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

### Giáº£i thÃ­ch vai trÃ² tá»«ng báº£ng

| Báº£ng | Vai trÃ² |
|---|---|
| USERS | TÃ i khoáº£n, phÃ¢n biá»‡t CUSTOMER / OPERATOR / ADMIN qua `role` |
| REFRESH_TOKENS | Cáº¥p láº¡i access token JWT mÃ  khÃ´ng báº¯t Ä‘Äƒng nháº­p láº¡i; há»— trá»£ logout/thu há»“i |
| VENUES | Äá»‹a Ä‘iá»ƒm tá»• chá»©c, tÃ¡i sá»­ dá»¥ng cho nhiá»u concert |
| CONCERTS | Sá»± kiá»‡n concert, `status`: DRAFT / PUBLISHED / CLOSED / CANCELLED |
| TICKET_CATEGORIES | Loáº¡i vÃ© (VIP/Standard), `type`: SEATED / STANDING â€” trung tÃ¢m cá»§a cÆ¡ cháº¿ chá»‘ng oversell |
| SEATS | Tá»«ng gháº¿ VIP cá»¥ thá»ƒ, chá»‰ Ã¡p dá»¥ng cho category SEATED |
| VOUCHERS | MÃ£ giáº£m giÃ¡, giá»›i háº¡n qua `max_usage` / `current_usage` |
| BOOKINGS | ÄÆ¡n Ä‘áº·t vÃ©, trung tÃ¢m ná»‘i Userâ€“Concertâ€“Voucher, chá»‘ng duplicate qua `idempotency_key` |
| BOOKING_ITEMS | Chi tiáº¿t tá»«ng vÃ© trong 1 booking (seat_id cho VIP, quantity cho Standard) |
| PAYMENTS | Tráº¡ng thÃ¡i thanh toÃ¡n, tÃ¡ch riÃªng Ä‘á»ƒ dá»… Ä‘Ã¡nh dáº¥u lÃ  mock |
| NOTIFICATIONS | ThÃ´ng bÃ¡o trong app |
| EMAIL_LOGS | Log gá»­i email xÃ¡c nháº­n/hoÃ n tiá»n |

---

## 4. PhÃ¢n loáº¡i vÃ©: VIP (SEATED) vs Standard (STANDING)

| | VIP â€” SEATED | Standard â€” STANDING |
|---|---|---|
| Khi Ä‘áº·t | Chá»n 1 gháº¿ cá»¥ thá»ƒ (seat_id) | Chá»‰ chá»n sá»‘ lÆ°á»£ng (quantity) |
| Chá»‘ng oversell | Lock tá»«ng dÃ²ng SEATS | Atomic decrement `available_quantity` |
| BOOKING_ITEMS | `seat_id` cÃ³ giÃ¡ trá»‹, quantity = 1 | `seat_id` = null, quantity > 1 |

NhÃ¡nh ráº½ trong service layer:
```java
if (ticketCategory.getType() == TicketType.SEATED) {
    // Lock gháº¿ cá»¥ thá»ƒ qua seat_id, kiá»ƒm tra status = AVAILABLE
} else { // STANDING
    // UPDATE ticket_categories SET available_quantity = available_quantity - :qty
    // WHERE id = :id AND available_quantity >= :qty
}
```

**Assumption:** má»—i concert seed tá»‘i Ä‘a 2 loáº¡i vÃ© (VIP, Standard). Kiáº¿n trÃºc há»— trá»£ má»Ÿ rá»™ng thÃªm category khÃ¡c nhá» field `type`, nhÆ°ng pháº¡m vi bÃ i test chá»‰ dÃ¹ng 2 loáº¡i nÃ y.

---

## 5. CÆ¡ cháº¿ chá»‘ng Oversell â€” Pessimistic Lock

**Lá»±a chá»n:** `SELECT ... FOR UPDATE` (Pessimistic Lock), khÃ´ng dÃ¹ng Optimistic Lock (version-based).

### So sÃ¡nh vÃ  lÃ½ do quyáº¿t Ä‘á»‹nh

| TiÃªu chÃ­ | Pessimistic Lock | Optimistic Lock |
|---|---|---|
| Hiá»‡u nÄƒng khi Ã­t conflict | Cháº­m hÆ¡n | Nhanh hÆ¡n |
| Hiá»‡u nÄƒng khi nhiá»u conflict (flash sale) | á»”n Ä‘á»‹nh, dá»… Ä‘oÃ¡n | Retry storm, tÄƒng táº£i Ä‘Ãºng lÃºc cáº§n á»•n Ä‘á»‹nh nháº¥t |
| Äá»™ phá»©c táº¡p code | ÄÆ¡n giáº£n, transaction tá»± lo | Phá»©c táº¡p hÆ¡n, pháº£i tá»± viáº¿t retry logic |
| Äá»™ cháº¯c cháº¯n | ThÃ nh cÃ´ng/tháº¥t báº¡i rÃµ rÃ ng ngay láº§n Ä‘áº§u | CÃ³ thá»ƒ pháº£i thá»­ láº¡i nhiá»u láº§n |

**LÃ½ do chá»n Pessimistic Lock:**
1. BÃ i toÃ¡n cÃ³ tÃ­nh cháº¥t "tÃ i nguyÃªn khan hiáº¿m, tranh cháº¥p cao" Ä‘Ãºng lÃºc flash sale â€” ká»‹ch báº£n mÃ  Optimistic Lock hoáº¡t Ä‘á»™ng kÃ©m nháº¥t do tá»· lá»‡ conflict cao.
2. Traffic thá»±c táº¿ (~8-9 req/giÃ¢y) hoÃ n toÃ n náº±m trong kháº£ nÄƒng xá»­ lÃ½ tá»‘t cá»§a Pessimistic Lock, chÆ°a Ä‘áº¿n má»©c cáº§n cÃ¢n nháº¯c giáº£i phÃ¡p khÃ¡c.
3. Äáº£m báº£o tÃ­nh Ä‘Ãºng Ä‘áº¯n tuyá»‡t Ä‘á»‘i, dá»… chá»©ng minh vÃ  test â€” quan trá»ng khi thá»i gian phÃ¡t triá»ƒn giá»›i háº¡n.

### Ãp dá»¥ng cá»¥ thá»ƒ

**VIP (SEATED):**
```sql
SELECT * FROM seats WHERE id = :seatId AND status = 'AVAILABLE' FOR UPDATE;
-- náº¿u láº¥y Ä‘Æ°á»£c row â†’ UPDATE status = 'HELD', held_until = now() + 5 phÃºt
-- náº¿u khÃ´ng láº¥y Ä‘Æ°á»£c (Ä‘ang bá»‹ lock) â†’ bÃ¡o "gháº¿ Ä‘Ã£ Ä‘Æ°á»£c ngÆ°á»i khÃ¡c giá»¯"
```

**Standard (STANDING):**
```sql
SELECT * FROM ticket_categories WHERE id = :categoryId FOR UPDATE;
-- kiá»ƒm tra available_quantity >= quantity trong cÃ¹ng transaction rá»“i trá»«
```

---

## 6. CÆ¡ cháº¿ chá»‘ng Duplicate Booking â€” Idempotency Key

**Váº¥n Ä‘á»:** khÃ¡ch báº¥m "Äáº·t vÃ©" nhÆ°ng timeout/máº¡ng cháº­m, báº¥m láº¡i â†’ táº¡o 2 booking trÃ¹ng cho cÃ¹ng 1 yÃªu cáº§u.

**Thiáº¿t káº¿:**

1. Client tá»± sinh UUID **trÆ°á»›c khi gá»­i request**, gá»­i qua header `Idempotency-Key`. Náº¿u báº¥m láº¡i do timeout, gá»­i láº¡i **cÃ¹ng key Ä‘Ã³**.
2. RÃ ng buá»™c unique á»Ÿ táº§ng database:
```sql
ALTER TABLE bookings ADD CONSTRAINT uq_idempotency_key UNIQUE (idempotency_key);
```
3. Xá»­ lÃ½ race condition á»Ÿ táº§ng code â€” khÃ´ng chá»‰ SELECT-rá»“i-INSERT, mÃ  báº¯t lá»—i unique violation:
```java
try {
    Booking booking = bookingRepository.save(newBooking);
} catch (DataIntegrityViolationException e) {
    Booking existing = bookingRepository.findByIdempotencyKey(key);
    return existing; // tráº£ vá» booking Ä‘Ã£ tá»“n táº¡i, khÃ´ng táº¡o trÃ¹ng
}
```

**Assumption:** idempotency key hiá»‡n khÃ´ng cÃ³ cÆ¡ cháº¿ háº¿t háº¡n/dá»n dáº¹p tá»± Ä‘á»™ng. Náº¿u triá»ƒn khai production cáº§n thÃªm scheduled job xÃ³a key cÅ© hÆ¡n 24â€“48h.

---

## 7. CÆ¡ cháº¿ chá»‘ng láº¡m dá»¥ng Voucher â€” Atomic Update

**Váº¥n Ä‘á» (Lost Update):** nhiá»u request cÃ¹ng Ä‘á»c `current_usage`, cÃ¹ng cá»™ng dá»“n vÃ  ghi Ä‘Ã¨ lÃªn nhau â†’ sá»‘ lÆ°á»£t dÃ¹ng thá»±c táº¿ bá»‹ "nuá»‘t máº¥t", vÆ°á»£t quÃ¡ `max_usage` mÃ  há»‡ thá»‘ng khÃ´ng phÃ¡t hiá»‡n.

**Giáº£i phÃ¡p:** gá»™p Ä‘á»c vÃ  ghi thÃ nh 1 cÃ¢u lá»‡nh atomic táº¡i database, khÃ´ng tÃ¡ch thÃ nh 2 bÆ°á»›c á»Ÿ táº§ng application:
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
    throw new VoucherLimitExceededException("Voucher Ä‘Ã£ háº¿t lÆ°á»£t sá»­ dá»¥ng");
}
```

`rowsAffected == 0` nghÄ©a lÃ  Ä‘iá»u kiá»‡n sai táº¡i thá»i Ä‘iá»ƒm ghi (Ä‘Ã£ háº¿t slot) â†’ tá»« chá»‘i ngay. Voucher Ä‘Æ°á»£c apply trong cÃ¹ng transaction vá»›i bÆ°á»›c lock gháº¿/trá»« vÃ© â€” náº¿u bÆ°á»›c nÃ o fail thÃ¬ rollback toÃ n bá»™.

---

## 8. State Machine

### 8.1 Booking

```
Received â†’ Pending payment â†’ Completed         (thanh toÃ¡n thÃ nh cÃ´ng)
                            â†’ Expired            (háº¿t 5 phÃºt chÆ°a thanh toÃ¡n)
                            â†’ Failed              (thanh toÃ¡n lá»—i)
Completed â†’ Refunded                              (chá»‰ khi operator há»§y toÃ n bá»™ concert â€” bulk refund)
```

**Assumption quan trá»ng:** há»‡ thá»‘ng khÃ´ng há»— trá»£ khÃ¡ch hÃ ng tá»± há»§y/yÃªu cáº§u hoÃ n tiá»n booking Ä‘Æ¡n láº» (chÃ­nh sÃ¡ch "no refund, no cancel" Ä‘Ãºng thá»±c táº¿ ngÃ nh ticketing). `Refunded` chá»‰ xáº£y ra khi operator há»§y toÃ n bá»™ concert, Ã¡p dá»¥ng hÃ ng loáº¡t cho cÃ¡c booking Ä‘Ã£ `Completed`.

### 8.2 Seat (chá»‰ Ã¡p dá»¥ng cho vÃ© VIP â€” SEATED)

```
Available â†’ Held (giá»¯ 5 phÃºt, khi khÃ¡ch chá»n gháº¿)
Held â†’ Booked (khi thanh toÃ¡n xong)
Held â†’ Available (tá»± Ä‘á»™ng, khi háº¿t 5 phÃºt â€” do Scheduled Job xá»­ lÃ½)
```

### 8.3 Má»‘i liÃªn káº¿t giá»¯a 2 state machine

| Booking chuyá»ƒn | Seat/Ticket category tÆ°Æ¡ng á»©ng |
|---|---|
| Received â†’ Pending payment | Seat: Available â†’ Held (VIP) / trá»« available_quantity (Standard) |
| â†’ Completed | Seat: Held â†’ Booked |
| â†’ Expired / Failed | Seat: Held â†’ Available (nháº£ láº¡i) / cá»™ng tráº£ available_quantity |

### 8.4 CÆ¡ cháº¿ tá»± Ä‘á»™ng nháº£ gháº¿ sau 5 phÃºt

**Chá»n: Scheduled Job** (thay vÃ¬ Redis TTL + keyspace notification).

LÃ½ do: Ä‘Æ¡n giáº£n hÆ¡n Ä‘á»ƒ implement vÃ  giáº£i thÃ­ch trong thá»i gian giá»›i háº¡n, Ä‘á»§ chÃ­nh xÃ¡c cho scope bÃ i test (Ä‘á»™ trá»… tá»‘i Ä‘a ~1 phÃºt khÃ´ng pháº£i váº¥n Ä‘á» lá»›n á»Ÿ quy mÃ´ traffic nÃ y). ÄÃ£ cÃ¢n nháº¯c Redis TTL (cÃ³ kinh nghiá»‡m tá»« dá»± Ã¡n ShiftSync) nhÆ°ng chá»n Scheduled Job vÃ¬ Ä‘Æ¡n giáº£n vÃ  Ä‘á»§ Ä‘Ã¡p á»©ng scale bÃ i toÃ¡n.

```java
@Scheduled(fixedRate = 60000) // cháº¡y má»—i 1 phÃºt
public void releaseExpiredHolds() {
    // QuÃ©t SEATS cÃ³ status = HELD vÃ  held_until < now()
    // â†’ set status = AVAILABLE, held_by_booking_id = null
    // â†’ set booking liÃªn quan sang status = EXPIRED
}
```

---

## 9. Luá»“ng xá»­ lÃ½ chÃ­nh

### 9.1 Customer â€” Ä‘áº·t vÃ©
```
Xem concert â†’ Chá»n loáº¡i vÃ©
  â†’ VIP: chá»n gháº¿ cá»¥ thá»ƒ
  â†’ Standard: chá»n sá»‘ lÆ°á»£ng
â†’ Táº¡o booking (idempotency key, lock gháº¿/vÃ© trong transaction)
â†’ Ãp voucher (tÃ¹y chá»n, atomic update)
â†’ Thanh toÃ¡n trong 5 phÃºt
```

### 9.2 Operation â€” váº­n hÃ nh
```
Publish concert / táº¡o loáº¡i vÃ©
Xem danh sÃ¡ch booking, filter theo tráº¡ng thÃ¡i
Update tráº¡ng thÃ¡i thá»§ cÃ´ng (xÃ¡c nháº­n thanh toÃ¡n thá»§ cÃ´ng / Ä‘Ã¡nh dáº¥u nghi váº¥n gian láº­n)
Há»§y concert â†’ bulk refund tá»± Ä‘á»™ng cho cÃ¡c booking Completed
```

---

## 10. Danh sÃ¡ch API endpoints

### Customer-facing (`/api/v1/customer`)

**Auth**
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`

**Concert & vÃ©**
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
- `GET /vouchers` (chá»‰ xem, khÃ´ng CRUD â€” seed-only)

---

## 11. TÃ³m táº¯t Assumptions / Scope

**ÄÃ£ lÃ m (in scope):**
- Kiáº¿n trÃºc monolith, tÃ¡ch logic theo package customer/operation
- Chá»‘ng oversell báº±ng Pessimistic Lock cho cáº£ 2 loáº¡i vÃ© (SEATED/STANDING)
- Chá»‘ng duplicate booking báº±ng idempotency key (unique constraint + exception handling)
- Chá»‘ng láº¡m dá»¥ng voucher báº±ng atomic UPDATE
- CÆ¡ cháº¿ giá»¯ gháº¿ VIP 5 phÃºt, tá»± Ä‘á»™ng nháº£ qua Scheduled Job
- State machine Ä‘áº§y Ä‘á»§ cho Booking (5 tráº¡ng thÃ¡i + Refunded) vÃ  Seat (3 tráº¡ng thÃ¡i)
- Bulk refund khi operator há»§y concert

**ChÆ°a lÃ m (out of scope, cÃ³ thá»ƒ má»Ÿ rá»™ng sau):**
- CRUD voucher tá»« operation dashboard â€” chá»‰ seed data qua Flyway migration
- TÃ­ch há»£p cá»•ng thanh toÃ¡n tháº­t (VNPay, Momo...) â€” chá»‰ mock
- KhÃ¡ch hÃ ng tá»± há»§y/yÃªu cáº§u hoÃ n tiá»n booking Ä‘Æ¡n láº» â€” chÃ­nh sÃ¡ch no refund, no cancel
- CÆ¡ cháº¿ háº¿t háº¡n/dá»n dáº¹p tá»± Ä‘á»™ng cho idempotency key
- Redis TTL cho viá»‡c nháº£ gháº¿ (Ä‘Ã£ cÃ¢n nháº¯c, chá»n Scheduled Job vÃ¬ Ä‘Æ¡n giáº£n hÆ¡n vÃ  Ä‘á»§ Ä‘Ã¡p á»©ng scale)
- Real-time push notification â€” Notification chá»‰ lÆ°u record, khÃ´ng cÃ³ WebSocket/SignalR
- Rate limiting nÃ¢ng cao á»Ÿ táº§ng API gateway
