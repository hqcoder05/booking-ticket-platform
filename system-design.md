# Concert Ticket Booking Platform Ã¢â‚¬â€ System & Database Design

**TÃƒÂ¡c giÃ¡ÂºÂ£:** HoÃƒÂ ng NguyÃ¡Â»â€¦n ViÃ¡ÂºÂ¿t QuÃ¡Â»â€˜c
**VÃ¡Â»â€¹ trÃƒÂ­ Ã¡Â»Â©ng tuyÃ¡Â»Æ’n:** Product Backend Intern Ã¢â‚¬â€ GEEK Up Geek Internship Autumn 2026

---

## 1. Business context tÃƒÂ³m tÃ¡ÂºÂ¯t

NÃ¡Â»Ân tÃ¡ÂºÂ£ng Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ© concert online, gÃ¡Â»â€œm 2 luÃ¡Â»â€œng:
- **Customer-facing**: browse concert, xem loÃ¡ÂºÂ¡i vÃƒÂ©, Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ©, ÃƒÂ¡p voucher, theo dÃƒÂµi trÃ¡ÂºÂ¡ng thÃƒÂ¡i booking
- **Operation Dashboard**: monitor booking, publish concert/vÃƒÂ©, quÃ¡ÂºÂ£n lÃƒÂ½ voucher (seed-only), xÃ¡Â»Â­ lÃƒÂ½ booking lÃ¡Â»â€”i, cÃ¡ÂºÂ­p nhÃ¡ÂºÂ­t trÃ¡ÂºÂ¡ng thÃƒÂ¡i thÃ¡Â»Â§ cÃƒÂ´ng

**RÃƒÂ ng buÃ¡Â»â„¢c kÃ¡Â»Â¹ thuÃ¡ÂºÂ­t quan trÃ¡Â»Âng:**
- Traffic Ã„â€˜Ã¡Â»â€°nh: ~50,000 users, 300Ã¢â‚¬â€œ500 booking request/phÃƒÂºt trong flash sale
- PhÃ¡ÂºÂ£i chÃ¡Â»â€˜ng: oversell vÃƒÂ©, duplicate booking do retry, lÃ¡ÂºÂ¡m dÃ¡Â»Â¥ng voucher, sÃ¡ÂºÂ­p hÃ¡Â»â€¡ thÃ¡Â»â€˜ng khi traffic tÃ„Æ’ng Ã„â€˜Ã¡Â»â„¢t biÃ¡ÂºÂ¿n

---

## 2. KiÃ¡ÂºÂ¿n trÃƒÂºc tÃ¡Â»â€¢ng quan

**LÃ¡Â»Â±a chÃ¡Â»Ân: Monolith, tÃƒÂ¡ch logic theo package (customer / operation)**

LÃƒÂ½ do:
- Traffic Ã„â€˜Ã¡Â»â€°nh thÃ¡Â»Â±c tÃ¡ÂºÂ¿ chÃ¡Â»â€° ~8Ã¢â‚¬â€œ9 request/giÃƒÂ¢y (300Ã¢â‚¬â€œ500/phÃƒÂºt) Ã¢â‚¬â€ khÃƒÂ´ng cÃ¡ÂºÂ§n microservices hay message queue phÃ¡Â»Â©c tÃ¡ÂºÂ¡p
- Monolith giÃ¡ÂºÂ£m Ã„â€˜Ã¡Â»â„¢ phÃ¡Â»Â©c tÃ¡ÂºÂ¡p vÃ¡ÂºÂ­n hÃƒÂ nh, phÃƒÂ¹ hÃ¡Â»Â£p vÃ¡Â»â€ºi thÃ¡Â»Âi gian phÃƒÂ¡t triÃ¡Â»Æ’n giÃ¡Â»â€ºi hÃ¡ÂºÂ¡n cÃ¡Â»Â§a bÃƒÂ i test
- VÃ¡ÂºÂ«n tÃƒÂ¡ch rÃƒÂµ rÃƒÂ ng Ã¡Â»Å¸ tÃ¡ÂºÂ§ng code: `controller/customer/...` vÃƒÂ  `controller/operation/...`, mÃ¡Â»â€”i nhÃƒÂ³m cÃƒÂ³ prefix URL riÃƒÂªng vÃƒÂ  authorization riÃƒÂªng theo `role` (Spring Security `@PreAuthorize`)

**Tech stack:**
- Backend: Spring Boot 3 + Java 21
- Database: PostgreSQL + Flyway (migration & seed data)
- Containerization: Docker Compose
- Auth: Spring Security + JWT (access token + refresh token)
- API docs: Springdoc OpenAPI (Swagger UI)
- Testing: JUnit 5 + Postman collection

LÃƒÂ½ do chÃ¡Â»Ân stack: Ã„â€˜ÃƒÂ£ cÃƒÂ³ kinh nghiÃ¡Â»â€¡m triÃ¡Â»Æ’n khai thÃ¡Â»Â±c tÃ¡ÂºÂ¿ qua cÃƒÂ¡c dÃ¡Â»Â± ÃƒÂ¡n trÃ†Â°Ã¡Â»â€ºc (ShiftSync, LinkUp), giÃ¡ÂºÂ£m thÃ¡Â»Âi gian setup, tÃ¡ÂºÂ­p trung thÃ¡Â»Âi gian vÃƒÂ o phÃ¡ÂºÂ§n logic nghiÃ¡Â»â€¡p vÃ¡Â»Â¥ cÃ¡Â»â€˜t lÃƒÂµi.

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

### GiÃ¡ÂºÂ£i thÃƒÂ­ch vai trÃƒÂ² tÃ¡Â»Â«ng bÃ¡ÂºÂ£ng

| BÃ¡ÂºÂ£ng | Vai trÃƒÂ² |
|---|---|
| USERS | TÃƒÂ i khoÃ¡ÂºÂ£n, phÃƒÂ¢n biÃ¡Â»â€¡t CUSTOMER / OPERATOR / ADMIN qua `role` |
| REFRESH_TOKENS | CÃ¡ÂºÂ¥p lÃ¡ÂºÂ¡i access token JWT mÃƒÂ  khÃƒÂ´ng bÃ¡ÂºÂ¯t Ã„â€˜Ã„Æ’ng nhÃ¡ÂºÂ­p lÃ¡ÂºÂ¡i; hÃ¡Â»â€” trÃ¡Â»Â£ logout/thu hÃ¡Â»â€œi |
| VENUES | Ã„ÂÃ¡Â»â€¹a Ã„â€˜iÃ¡Â»Æ’m tÃ¡Â»â€¢ chÃ¡Â»Â©c, tÃƒÂ¡i sÃ¡Â»Â­ dÃ¡Â»Â¥ng cho nhiÃ¡Â»Âu concert |
| CONCERTS | SÃ¡Â»Â± kiÃ¡Â»â€¡n concert, `status`: DRAFT / PUBLISHED / CLOSED / CANCELLED |
| TICKET_CATEGORIES | LoÃ¡ÂºÂ¡i vÃƒÂ© (VIP/Standard), `type`: SEATED / STANDING Ã¢â‚¬â€ trung tÃƒÂ¢m cÃ¡Â»Â§a cÃ†Â¡ chÃ¡ÂºÂ¿ chÃ¡Â»â€˜ng oversell |
| SEATS | TÃ¡Â»Â«ng ghÃ¡ÂºÂ¿ VIP cÃ¡Â»Â¥ thÃ¡Â»Æ’, chÃ¡Â»â€° ÃƒÂ¡p dÃ¡Â»Â¥ng cho category SEATED |
| VOUCHERS | MÃƒÂ£ giÃ¡ÂºÂ£m giÃƒÂ¡, giÃ¡Â»â€ºi hÃ¡ÂºÂ¡n qua `max_usage` / `current_usage` |
| BOOKINGS | Ã„ÂÃ†Â¡n Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ©, trung tÃƒÂ¢m nÃ¡Â»â€˜i UserÃ¢â‚¬â€œConcertÃ¢â‚¬â€œVoucher, chÃ¡Â»â€˜ng duplicate qua `idempotency_key` |
| BOOKING_ITEMS | Chi tiÃ¡ÂºÂ¿t tÃ¡Â»Â«ng vÃƒÂ© trong 1 booking (seat_id cho VIP, quantity cho Standard) |
| PAYMENTS | TrÃ¡ÂºÂ¡ng thÃƒÂ¡i thanh toÃƒÂ¡n, tÃƒÂ¡ch riÃƒÂªng Ã„â€˜Ã¡Â»Æ’ dÃ¡Â»â€¦ Ã„â€˜ÃƒÂ¡nh dÃ¡ÂºÂ¥u lÃƒÂ  mock |
| NOTIFICATIONS | ThÃƒÂ´ng bÃƒÂ¡o trong app |
| EMAIL_LOGS | Log gÃ¡Â»Â­i email xÃƒÂ¡c nhÃ¡ÂºÂ­n/hoÃƒÂ n tiÃ¡Â»Ân |

---

## 4. PhÃƒÂ¢n loÃ¡ÂºÂ¡i vÃƒÂ©: VIP (SEATED) vs Standard (STANDING)

| | VIP Ã¢â‚¬â€ SEATED | Standard Ã¢â‚¬â€ STANDING |
|---|---|---|
| Khi Ã„â€˜Ã¡ÂºÂ·t | ChÃ¡Â»Ân 1 ghÃ¡ÂºÂ¿ cÃ¡Â»Â¥ thÃ¡Â»Æ’ (seat_id) | ChÃ¡Â»â€° chÃ¡Â»Ân sÃ¡Â»â€˜ lÃ†Â°Ã¡Â»Â£ng (quantity) |
| ChÃ¡Â»â€˜ng oversell | Lock tÃ¡Â»Â«ng dÃƒÂ²ng SEATS | Atomic decrement `available_quantity` |
| BOOKING_ITEMS | `seat_id` cÃƒÂ³ giÃƒÂ¡ trÃ¡Â»â€¹, quantity = 1 | `seat_id` = null, quantity > 1 |

NhÃƒÂ¡nh rÃ¡ÂºÂ½ trong service layer:
```java
if (ticketCategory.getType() == TicketType.SEATED) {
    // Lock ghÃ¡ÂºÂ¿ cÃ¡Â»Â¥ thÃ¡Â»Æ’ qua seat_id, kiÃ¡Â»Æ’m tra status = AVAILABLE
} else { // STANDING
    // UPDATE ticket_categories SET available_quantity = available_quantity - :qty
    // WHERE id = :id AND available_quantity >= :qty
}
```

**Assumption:** mÃ¡Â»â€”i concert seed tÃ¡Â»â€˜i Ã„â€˜a 2 loÃ¡ÂºÂ¡i vÃƒÂ© (VIP, Standard). KiÃ¡ÂºÂ¿n trÃƒÂºc hÃ¡Â»â€” trÃ¡Â»Â£ mÃ¡Â»Å¸ rÃ¡Â»â„¢ng thÃƒÂªm category khÃƒÂ¡c nhÃ¡Â»Â field `type`, nhÃ†Â°ng phÃ¡ÂºÂ¡m vi bÃƒÂ i test chÃ¡Â»â€° dÃƒÂ¹ng 2 loÃ¡ÂºÂ¡i nÃƒÂ y.

---

## 5. CÃ†Â¡ chÃ¡ÂºÂ¿ chÃ¡Â»â€˜ng Oversell Ã¢â‚¬â€ Pessimistic Lock

**LÃ¡Â»Â±a chÃ¡Â»Ân:** `SELECT ... FOR UPDATE` (Pessimistic Lock), khÃƒÂ´ng dÃƒÂ¹ng Optimistic Lock (version-based).

### So sÃƒÂ¡nh vÃƒÂ  lÃƒÂ½ do quyÃ¡ÂºÂ¿t Ã„â€˜Ã¡Â»â€¹nh

| TiÃƒÂªu chÃƒÂ­ | Pessimistic Lock | Optimistic Lock |
|---|---|---|
| HiÃ¡Â»â€¡u nÃ„Æ’ng khi ÃƒÂ­t conflict | ChÃ¡ÂºÂ­m hÃ†Â¡n | Nhanh hÃ†Â¡n |
| HiÃ¡Â»â€¡u nÃ„Æ’ng khi nhiÃ¡Â»Âu conflict (flash sale) | Ã¡Â»â€n Ã„â€˜Ã¡Â»â€¹nh, dÃ¡Â»â€¦ Ã„â€˜oÃƒÂ¡n | Retry storm, tÃ„Æ’ng tÃ¡ÂºÂ£i Ã„â€˜ÃƒÂºng lÃƒÂºc cÃ¡ÂºÂ§n Ã¡Â»â€¢n Ã„â€˜Ã¡Â»â€¹nh nhÃ¡ÂºÂ¥t |
| Ã„ÂÃ¡Â»â„¢ phÃ¡Â»Â©c tÃ¡ÂºÂ¡p code | Ã„ÂÃ†Â¡n giÃ¡ÂºÂ£n, transaction tÃ¡Â»Â± lo | PhÃ¡Â»Â©c tÃ¡ÂºÂ¡p hÃ†Â¡n, phÃ¡ÂºÂ£i tÃ¡Â»Â± viÃ¡ÂºÂ¿t retry logic |
| Ã„ÂÃ¡Â»â„¢ chÃ¡ÂºÂ¯c chÃ¡ÂºÂ¯n | ThÃƒÂ nh cÃƒÂ´ng/thÃ¡ÂºÂ¥t bÃ¡ÂºÂ¡i rÃƒÂµ rÃƒÂ ng ngay lÃ¡ÂºÂ§n Ã„â€˜Ã¡ÂºÂ§u | CÃƒÂ³ thÃ¡Â»Æ’ phÃ¡ÂºÂ£i thÃ¡Â»Â­ lÃ¡ÂºÂ¡i nhiÃ¡Â»Âu lÃ¡ÂºÂ§n |

**LÃƒÂ½ do chÃ¡Â»Ân Pessimistic Lock:**
1. BÃƒÂ i toÃƒÂ¡n cÃƒÂ³ tÃƒÂ­nh chÃ¡ÂºÂ¥t "tÃƒÂ i nguyÃƒÂªn khan hiÃ¡ÂºÂ¿m, tranh chÃ¡ÂºÂ¥p cao" Ã„â€˜ÃƒÂºng lÃƒÂºc flash sale Ã¢â‚¬â€ kÃ¡Â»â€¹ch bÃ¡ÂºÂ£n mÃƒÂ  Optimistic Lock hoÃ¡ÂºÂ¡t Ã„â€˜Ã¡Â»â„¢ng kÃƒÂ©m nhÃ¡ÂºÂ¥t do tÃ¡Â»Â· lÃ¡Â»â€¡ conflict cao.
2. Traffic thÃ¡Â»Â±c tÃ¡ÂºÂ¿ (~8-9 req/giÃƒÂ¢y) hoÃƒÂ n toÃƒÂ n nÃ¡ÂºÂ±m trong khÃ¡ÂºÂ£ nÃ„Æ’ng xÃ¡Â»Â­ lÃƒÂ½ tÃ¡Â»â€˜t cÃ¡Â»Â§a Pessimistic Lock, chÃ†Â°a Ã„â€˜Ã¡ÂºÂ¿n mÃ¡Â»Â©c cÃ¡ÂºÂ§n cÃƒÂ¢n nhÃ¡ÂºÂ¯c giÃ¡ÂºÂ£i phÃƒÂ¡p khÃƒÂ¡c.
3. Ã„ÂÃ¡ÂºÂ£m bÃ¡ÂºÂ£o tÃƒÂ­nh Ã„â€˜ÃƒÂºng Ã„â€˜Ã¡ÂºÂ¯n tuyÃ¡Â»â€¡t Ã„â€˜Ã¡Â»â€˜i, dÃ¡Â»â€¦ chÃ¡Â»Â©ng minh vÃƒÂ  test Ã¢â‚¬â€ quan trÃ¡Â»Âng khi thÃ¡Â»Âi gian phÃƒÂ¡t triÃ¡Â»Æ’n giÃ¡Â»â€ºi hÃ¡ÂºÂ¡n.

### ÃƒÂp dÃ¡Â»Â¥ng cÃ¡Â»Â¥ thÃ¡Â»Æ’

**VIP (SEATED):**
```sql
SELECT * FROM seats WHERE id = :seatId AND status = 'AVAILABLE' FOR UPDATE;
-- nÃ¡ÂºÂ¿u lÃ¡ÂºÂ¥y Ã„â€˜Ã†Â°Ã¡Â»Â£c row Ã¢â€ â€™ UPDATE status = 'HELD', held_until = now() + 5 phÃƒÂºt
-- nÃ¡ÂºÂ¿u khÃƒÂ´ng lÃ¡ÂºÂ¥y Ã„â€˜Ã†Â°Ã¡Â»Â£c (Ã„â€˜ang bÃ¡Â»â€¹ lock) Ã¢â€ â€™ bÃƒÂ¡o "ghÃ¡ÂºÂ¿ Ã„â€˜ÃƒÂ£ Ã„â€˜Ã†Â°Ã¡Â»Â£c ngÃ†Â°Ã¡Â»Âi khÃƒÂ¡c giÃ¡Â»Â¯"
```

**Standard (STANDING):**
```sql
SELECT * FROM ticket_categories WHERE id = :categoryId FOR UPDATE;
-- kiÃ¡Â»Æ’m tra available_quantity >= quantity trong cÃƒÂ¹ng transaction rÃ¡Â»â€œi trÃ¡Â»Â«
```

---

## 6. CÃ†Â¡ chÃ¡ÂºÂ¿ chÃ¡Â»â€˜ng Duplicate Booking Ã¢â‚¬â€ Idempotency Key

**VÃ¡ÂºÂ¥n Ã„â€˜Ã¡Â»Â:** khÃƒÂ¡ch bÃ¡ÂºÂ¥m "Ã„ÂÃ¡ÂºÂ·t vÃƒÂ©" nhÃ†Â°ng timeout/mÃ¡ÂºÂ¡ng chÃ¡ÂºÂ­m, bÃ¡ÂºÂ¥m lÃ¡ÂºÂ¡i Ã¢â€ â€™ tÃ¡ÂºÂ¡o 2 booking trÃƒÂ¹ng cho cÃƒÂ¹ng 1 yÃƒÂªu cÃ¡ÂºÂ§u.

**ThiÃ¡ÂºÂ¿t kÃ¡ÂºÂ¿:**

1. Client tÃ¡Â»Â± sinh UUID **trÃ†Â°Ã¡Â»â€ºc khi gÃ¡Â»Â­i request**, gÃ¡Â»Â­i qua header `Idempotency-Key`. NÃ¡ÂºÂ¿u bÃ¡ÂºÂ¥m lÃ¡ÂºÂ¡i do timeout, gÃ¡Â»Â­i lÃ¡ÂºÂ¡i **cÃƒÂ¹ng key Ã„â€˜ÃƒÂ³**.
2. RÃƒÂ ng buÃ¡Â»â„¢c unique Ã¡Â»Å¸ tÃ¡ÂºÂ§ng database:
```sql
ALTER TABLE bookings ADD CONSTRAINT uq_idempotency_key UNIQUE (idempotency_key);
```
3. XÃ¡Â»Â­ lÃƒÂ½ race condition Ã¡Â»Å¸ tÃ¡ÂºÂ§ng code Ã¢â‚¬â€ khÃƒÂ´ng chÃ¡Â»â€° SELECT-rÃ¡Â»â€œi-INSERT, mÃƒÂ  bÃ¡ÂºÂ¯t lÃ¡Â»â€”i unique violation:
```java
try {
    Booking booking = bookingRepository.save(newBooking);
} catch (DataIntegrityViolationException e) {
    Booking existing = bookingRepository.findByIdempotencyKey(key);
    return existing; // trÃ¡ÂºÂ£ vÃ¡Â»Â booking Ã„â€˜ÃƒÂ£ tÃ¡Â»â€œn tÃ¡ÂºÂ¡i, khÃƒÂ´ng tÃ¡ÂºÂ¡o trÃƒÂ¹ng
}
```

**Assumption:** idempotency key hiÃ¡Â»â€¡n khÃƒÂ´ng cÃƒÂ³ cÃ†Â¡ chÃ¡ÂºÂ¿ hÃ¡ÂºÂ¿t hÃ¡ÂºÂ¡n/dÃ¡Â»Ân dÃ¡ÂºÂ¹p tÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng. NÃ¡ÂºÂ¿u triÃ¡Â»Æ’n khai production cÃ¡ÂºÂ§n thÃƒÂªm scheduled job xÃƒÂ³a key cÃ…Â© hÃ†Â¡n 24Ã¢â‚¬â€œ48h.

---

## 7. CÃ†Â¡ chÃ¡ÂºÂ¿ chÃ¡Â»â€˜ng lÃ¡ÂºÂ¡m dÃ¡Â»Â¥ng Voucher Ã¢â‚¬â€ Atomic Update

**VÃ¡ÂºÂ¥n Ã„â€˜Ã¡Â»Â (Lost Update):** nhiÃ¡Â»Âu request cÃƒÂ¹ng Ã„â€˜Ã¡Â»Âc `current_usage`, cÃƒÂ¹ng cÃ¡Â»â„¢ng dÃ¡Â»â€œn vÃƒÂ  ghi Ã„â€˜ÃƒÂ¨ lÃƒÂªn nhau Ã¢â€ â€™ sÃ¡Â»â€˜ lÃ†Â°Ã¡Â»Â£t dÃƒÂ¹ng thÃ¡Â»Â±c tÃ¡ÂºÂ¿ bÃ¡Â»â€¹ "nuÃ¡Â»â€˜t mÃ¡ÂºÂ¥t", vÃ†Â°Ã¡Â»Â£t quÃƒÂ¡ `max_usage` mÃƒÂ  hÃ¡Â»â€¡ thÃ¡Â»â€˜ng khÃƒÂ´ng phÃƒÂ¡t hiÃ¡Â»â€¡n.

**GiÃ¡ÂºÂ£i phÃƒÂ¡p:** gÃ¡Â»â„¢p Ã„â€˜Ã¡Â»Âc vÃƒÂ  ghi thÃƒÂ nh 1 cÃƒÂ¢u lÃ¡Â»â€¡nh atomic tÃ¡ÂºÂ¡i database, khÃƒÂ´ng tÃƒÂ¡ch thÃƒÂ nh 2 bÃ†Â°Ã¡Â»â€ºc Ã¡Â»Å¸ tÃ¡ÂºÂ§ng application:
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
    throw new VoucherLimitExceededException("Voucher Ã„â€˜ÃƒÂ£ hÃ¡ÂºÂ¿t lÃ†Â°Ã¡Â»Â£t sÃ¡Â»Â­ dÃ¡Â»Â¥ng");
}
```

`rowsAffected == 0` nghÃ„Â©a lÃƒÂ  Ã„â€˜iÃ¡Â»Âu kiÃ¡Â»â€¡n sai tÃ¡ÂºÂ¡i thÃ¡Â»Âi Ã„â€˜iÃ¡Â»Æ’m ghi (Ã„â€˜ÃƒÂ£ hÃ¡ÂºÂ¿t slot) Ã¢â€ â€™ tÃ¡Â»Â« chÃ¡Â»â€˜i ngay. Voucher Ã„â€˜Ã†Â°Ã¡Â»Â£c apply trong cÃƒÂ¹ng transaction vÃ¡Â»â€ºi bÃ†Â°Ã¡Â»â€ºc lock ghÃ¡ÂºÂ¿/trÃ¡Â»Â« vÃƒÂ© Ã¢â‚¬â€ nÃ¡ÂºÂ¿u bÃ†Â°Ã¡Â»â€ºc nÃƒÂ o fail thÃƒÂ¬ rollback toÃƒÂ n bÃ¡Â»â„¢.

---

## 8. State Machine

### 8.1 Booking

```
Received Ã¢â€ â€™ Pending payment Ã¢â€ â€™ Completed         (thanh toÃƒÂ¡n thÃƒÂ nh cÃƒÂ´ng)
                            Ã¢â€ â€™ Expired            (hÃ¡ÂºÂ¿t 5 phÃƒÂºt chÃ†Â°a thanh toÃƒÂ¡n)
                            Ã¢â€ â€™ Failed              (thanh toÃƒÂ¡n lÃ¡Â»â€”i)
Completed Ã¢â€ â€™ Refunded                              (chÃ¡Â»â€° khi operator hÃ¡Â»Â§y toÃƒÂ n bÃ¡Â»â„¢ concert Ã¢â‚¬â€ bulk refund)
```

**Assumption quan trÃ¡Â»Âng:** hÃ¡Â»â€¡ thÃ¡Â»â€˜ng khÃƒÂ´ng hÃ¡Â»â€” trÃ¡Â»Â£ khÃƒÂ¡ch hÃƒÂ ng tÃ¡Â»Â± hÃ¡Â»Â§y/yÃƒÂªu cÃ¡ÂºÂ§u hoÃƒÂ n tiÃ¡Â»Ân booking Ã„â€˜Ã†Â¡n lÃ¡ÂºÂ» (chÃƒÂ­nh sÃƒÂ¡ch "no refund, no cancel" Ã„â€˜ÃƒÂºng thÃ¡Â»Â±c tÃ¡ÂºÂ¿ ngÃƒÂ nh ticketing). `Refunded` chÃ¡Â»â€° xÃ¡ÂºÂ£y ra khi operator hÃ¡Â»Â§y toÃƒÂ n bÃ¡Â»â„¢ concert, ÃƒÂ¡p dÃ¡Â»Â¥ng hÃƒÂ ng loÃ¡ÂºÂ¡t cho cÃƒÂ¡c booking Ã„â€˜ÃƒÂ£ `Completed`.

### 8.2 Seat (chÃ¡Â»â€° ÃƒÂ¡p dÃ¡Â»Â¥ng cho vÃƒÂ© VIP Ã¢â‚¬â€ SEATED)

```
Available Ã¢â€ â€™ Held (giÃ¡Â»Â¯ 5 phÃƒÂºt, khi khÃƒÂ¡ch chÃ¡Â»Ân ghÃ¡ÂºÂ¿)
Held Ã¢â€ â€™ Booked (khi thanh toÃƒÂ¡n xong)
Held Ã¢â€ â€™ Available (tÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng, khi hÃ¡ÂºÂ¿t 5 phÃƒÂºt Ã¢â‚¬â€ do Scheduled Job xÃ¡Â»Â­ lÃƒÂ½)
```

### 8.3 MÃ¡Â»â€˜i liÃƒÂªn kÃ¡ÂºÂ¿t giÃ¡Â»Â¯a 2 state machine

| Booking chuyÃ¡Â»Æ’n | Seat/Ticket category tÃ†Â°Ã†Â¡ng Ã¡Â»Â©ng |
|---|---|
| Received Ã¢â€ â€™ Pending payment | Seat: Available Ã¢â€ â€™ Held (VIP) / trÃ¡Â»Â« available_quantity (Standard) |
| Ã¢â€ â€™ Completed | Seat: Held Ã¢â€ â€™ Booked |
| Ã¢â€ â€™ Expired / Failed | Seat: Held Ã¢â€ â€™ Available (nhÃ¡ÂºÂ£ lÃ¡ÂºÂ¡i) / cÃ¡Â»â„¢ng trÃ¡ÂºÂ£ available_quantity |

### 8.4 CÃ†Â¡ chÃ¡ÂºÂ¿ tÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng nhÃ¡ÂºÂ£ ghÃ¡ÂºÂ¿ sau 5 phÃƒÂºt

**ChÃ¡Â»Ân: Scheduled Job** (thay vÃƒÂ¬ Redis TTL + keyspace notification).

LÃƒÂ½ do: Ã„â€˜Ã†Â¡n giÃ¡ÂºÂ£n hÃ†Â¡n Ã„â€˜Ã¡Â»Æ’ implement vÃƒÂ  giÃ¡ÂºÂ£i thÃƒÂ­ch trong thÃ¡Â»Âi gian giÃ¡Â»â€ºi hÃ¡ÂºÂ¡n, Ã„â€˜Ã¡Â»Â§ chÃƒÂ­nh xÃƒÂ¡c cho scope bÃƒÂ i test (Ã„â€˜Ã¡Â»â„¢ trÃ¡Â»â€¦ tÃ¡Â»â€˜i Ã„â€˜a ~1 phÃƒÂºt khÃƒÂ´ng phÃ¡ÂºÂ£i vÃ¡ÂºÂ¥n Ã„â€˜Ã¡Â»Â lÃ¡Â»â€ºn Ã¡Â»Å¸ quy mÃƒÂ´ traffic nÃƒÂ y). Ã„ÂÃƒÂ£ cÃƒÂ¢n nhÃ¡ÂºÂ¯c Redis TTL (cÃƒÂ³ kinh nghiÃ¡Â»â€¡m tÃ¡Â»Â« dÃ¡Â»Â± ÃƒÂ¡n ShiftSync) nhÃ†Â°ng chÃ¡Â»Ân Scheduled Job vÃƒÂ¬ Ã„â€˜Ã†Â¡n giÃ¡ÂºÂ£n vÃƒÂ  Ã„â€˜Ã¡Â»Â§ Ã„â€˜ÃƒÂ¡p Ã¡Â»Â©ng scale bÃƒÂ i toÃƒÂ¡n.

```java
@Scheduled(fixedRate = 60000) // chÃ¡ÂºÂ¡y mÃ¡Â»â€”i 1 phÃƒÂºt
public void releaseExpiredHolds() {
    // QuÃƒÂ©t SEATS cÃƒÂ³ status = HELD vÃƒÂ  held_until < now()
    // Ã¢â€ â€™ set status = AVAILABLE, held_by_booking_id = null
    // Ã¢â€ â€™ set booking liÃƒÂªn quan sang status = EXPIRED
}
```

---

## 9. LuÃ¡Â»â€œng xÃ¡Â»Â­ lÃƒÂ½ chÃƒÂ­nh

### 9.1 Customer Ã¢â‚¬â€ Ã„â€˜Ã¡ÂºÂ·t vÃƒÂ©
```
Xem concert Ã¢â€ â€™ ChÃ¡Â»Ân loÃ¡ÂºÂ¡i vÃƒÂ©
  Ã¢â€ â€™ VIP: chÃ¡Â»Ân ghÃ¡ÂºÂ¿ cÃ¡Â»Â¥ thÃ¡Â»Æ’
  Ã¢â€ â€™ Standard: chÃ¡Â»Ân sÃ¡Â»â€˜ lÃ†Â°Ã¡Â»Â£ng
Ã¢â€ â€™ TÃ¡ÂºÂ¡o booking (idempotency key, lock ghÃ¡ÂºÂ¿/vÃƒÂ© trong transaction)
Ã¢â€ â€™ ÃƒÂp voucher (tÃƒÂ¹y chÃ¡Â»Ân, atomic update)
Ã¢â€ â€™ Thanh toÃƒÂ¡n trong 5 phÃƒÂºt
```

### 9.2 Operation Ã¢â‚¬â€ vÃ¡ÂºÂ­n hÃƒÂ nh
```
Publish concert / tÃ¡ÂºÂ¡o loÃ¡ÂºÂ¡i vÃƒÂ©
Xem danh sÃƒÂ¡ch booking, filter theo trÃ¡ÂºÂ¡ng thÃƒÂ¡i
Update trÃ¡ÂºÂ¡ng thÃƒÂ¡i thÃ¡Â»Â§ cÃƒÂ´ng (xÃƒÂ¡c nhÃ¡ÂºÂ­n thanh toÃƒÂ¡n thÃ¡Â»Â§ cÃƒÂ´ng / Ã„â€˜ÃƒÂ¡nh dÃ¡ÂºÂ¥u nghi vÃ¡ÂºÂ¥n gian lÃ¡ÂºÂ­n)
HÃ¡Â»Â§y concert Ã¢â€ â€™ bulk refund tÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng cho cÃƒÂ¡c booking Completed
```

---

## 10. Danh sÃƒÂ¡ch API endpoints

### Customer-facing (`/api/v1/customer`)

**Auth**
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`

**Concert & vÃƒÂ©**
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
- `GET /vouchers` (chÃ¡Â»â€° xem, khÃƒÂ´ng CRUD Ã¢â‚¬â€ seed-only)

---

## 11. TÃƒÂ³m tÃ¡ÂºÂ¯t Assumptions / Scope

**Ã„ÂÃƒÂ£ lÃƒÂ m (in scope):**
- KiÃ¡ÂºÂ¿n trÃƒÂºc monolith, tÃƒÂ¡ch logic theo package customer/operation
- ChÃ¡Â»â€˜ng oversell bÃ¡ÂºÂ±ng Pessimistic Lock cho cÃ¡ÂºÂ£ 2 loÃ¡ÂºÂ¡i vÃƒÂ© (SEATED/STANDING)
- ChÃ¡Â»â€˜ng duplicate booking bÃ¡ÂºÂ±ng idempotency key (unique constraint + exception handling)
- ChÃ¡Â»â€˜ng lÃ¡ÂºÂ¡m dÃ¡Â»Â¥ng voucher bÃ¡ÂºÂ±ng atomic UPDATE
- CÃ†Â¡ chÃ¡ÂºÂ¿ giÃ¡Â»Â¯ ghÃ¡ÂºÂ¿ VIP 5 phÃƒÂºt, tÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng nhÃ¡ÂºÂ£ qua Scheduled Job
- State machine Ã„â€˜Ã¡ÂºÂ§y Ã„â€˜Ã¡Â»Â§ cho Booking (5 trÃ¡ÂºÂ¡ng thÃƒÂ¡i + Refunded) vÃƒÂ  Seat (3 trÃ¡ÂºÂ¡ng thÃƒÂ¡i)
- Bulk refund khi operator hÃ¡Â»Â§y concert

**ChÃ†Â°a lÃƒÂ m (out of scope, cÃƒÂ³ thÃ¡Â»Æ’ mÃ¡Â»Å¸ rÃ¡Â»â„¢ng sau):**
- CRUD voucher tÃ¡Â»Â« operation dashboard Ã¢â‚¬â€ chÃ¡Â»â€° seed data qua Flyway migration
- TÃƒÂ­ch hÃ¡Â»Â£p cÃ¡Â»â€¢ng thanh toÃƒÂ¡n thÃ¡ÂºÂ­t (VNPay, Momo...) Ã¢â‚¬â€ chÃ¡Â»â€° mock
- KhÃƒÂ¡ch hÃƒÂ ng tÃ¡Â»Â± hÃ¡Â»Â§y/yÃƒÂªu cÃ¡ÂºÂ§u hoÃƒÂ n tiÃ¡Â»Ân booking Ã„â€˜Ã†Â¡n lÃ¡ÂºÂ» Ã¢â‚¬â€ chÃƒÂ­nh sÃƒÂ¡ch no refund, no cancel
- CÃ†Â¡ chÃ¡ÂºÂ¿ hÃ¡ÂºÂ¿t hÃ¡ÂºÂ¡n/dÃ¡Â»Ân dÃ¡ÂºÂ¹p tÃ¡Â»Â± Ã„â€˜Ã¡Â»â„¢ng cho idempotency key
- Redis TTL cho viÃ¡Â»â€¡c nhÃ¡ÂºÂ£ ghÃ¡ÂºÂ¿ (Ã„â€˜ÃƒÂ£ cÃƒÂ¢n nhÃ¡ÂºÂ¯c, chÃ¡Â»Ân Scheduled Job vÃƒÂ¬ Ã„â€˜Ã†Â¡n giÃ¡ÂºÂ£n hÃ†Â¡n vÃƒÂ  Ã„â€˜Ã¡Â»Â§ Ã„â€˜ÃƒÂ¡p Ã¡Â»Â©ng scale)
- Real-time push notification Ã¢â‚¬â€ Notification chÃ¡Â»â€° lÃ†Â°u record, khÃƒÂ´ng cÃƒÂ³ WebSocket/SignalR
- Rate limiting nÃƒÂ¢ng cao Ã¡Â»Å¸ tÃ¡ÂºÂ§ng API gateway
