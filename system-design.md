# System & Database Design Document

This document outlines the architectural decisions, trade-offs, and reasoning behind the Concert Ticket Booking Platform.

## 1. How I Think About Backend Design

My approach to this backend design prioritizes **Data Integrity, Simplicity, and Defensibility** over premature optimization. Given the context of a 48-hour assessment and a startup launching a Flash Sale, my philosophy is:
- **Monolith First:** A well-structured Monolith (with logical separation of customer and operation domains) is far superior to a poorly executed Microservices architecture. It eliminates network latency between services and simplifies ACID transactions.
- **Let the Database Do the Heavy Lifting:** PostgreSQL is incredibly robust. Instead of introducing external distributed locks (Redis/Zookeeper) which add operational complexity, I utilized PostgreSQL's native row-level locking (SELECT FOR UPDATE) to guarantee consistency.

## 2. Database Design

The database is fully normalized and managed via **Flyway Migrations**. 
- **UUIDs as Primary Keys:** Prevents ID enumeration attacks and prepares the system for future horizontal partitioning.
- **Core Entities:** User, Concert, Venue, Seat (Inventory), Booking, Payment, Voucher.
- **Concurrency Support:** The Seat and Voucher tables act as the critical bottlenecks during a flash sale and are designed to be locked at the row level during transactions.

## 3. Assumptions & Scope (What is Done vs. What is NOT Done)

To deliver the greatest value within the time limit, I carefully defined the scope and made explicit business assumptions.

### What is IN Scope (Done):
- **4-State Booking Machine:** Bookings strictly follow the states: PENDING -> PAID -> CANCELLED (if 5 mins expire or manually cancelled) -> REFUNDED (if concert is aborted).
- **Idempotent API:** Safe retries for booking endpoints.
- **Seat Auto-Release:** A Scheduled Job runs every 5 seconds to automatically release PENDING seats back to AVAILABLE if payment isn't completed within 5 minutes.
- **Anti-Seat Hoarding (DoS Protection):** A strict rule where 1 User can only have a maximum of 1 PENDING booking at a time. This prevents malicious users from locking the entire venue's seats.

### What is OUT of Scope (Limitation / Not Done):
- **CRUD Operations for Vouchers:** As suggested in the requirements, the system does *not* provide Operation APIs to create/update/delete vouchers. Instead, vouchers are securely seeded into the database via Flyway, and the system focuses strictly on ensuring customers can apply them concurrently without abuse.
- **Real Payment Gateway Integration:** The /pay endpoint mocks a successful payment process to focus the assessment on order state transitions rather than 3rd-party SDK integration.
- **WebSockets for Real-time Seat Map:** Instead of a complex WebSocket setup, the frontend is assumed to use Polling (e.g., React Query refetching every 5s).

## 4. Reasoning About Possible Issues and Workflows

The startup explicitly highlighted concerns about a Flash Sale (50,000 users, 300-500 bookings/min). Here is how the system reasons about and mitigates these core issues:

### A. Overselling Tickets (Inventory Contention)
- **Issue:** Thousands of users clicking the same VIP seat simultaneously.
- **Solution:** Pessimistic Locking. The system uses @Lock(LockModeType.PESSIMISTIC_WRITE) when selecting a Seat. The first transaction acquires the database lock; subsequent transactions must wait and will immediately see the seat as PENDING once the first transaction commits, resulting in a clean failure for latecomers rather than double-booking.

### B. Duplicate Bookings Caused by Retries
- **Issue:** User's network drops, they retry the booking request, accidentally creating two bookings.
- **Solution:** Every booking request requires a client-generated Idempotency-Key (UUID) placed in the HTTP Header. The ookings table has a UNIQUE constraint on this key. A concurrent retry will trigger a DataIntegrityViolationException, which the global exception handler catches and safely rejects.

### C. Users Abusing Promotional Vouchers
- **Issue:** A voucher has max_usage = 100, but 10,000 users apply it at the exact same millisecond.
- **Solution:** Atomic updates. The voucher is fetched using a Pessimistic Write Lock (indByCodeForUpdate). The application checks current_usage < max_usage, increments it, and saves it within the same strict transaction boundary. (Proven via VoucherConcurrencyTest.java).

### D. System Instability During Flash Sale Spikes
- **Issue:** 50,000 users aggressively refreshing the page to view the seat map, causing Database CPU to spike to 100% and crashing the system.
- **Solution:** **Short-lived In-Memory Cache (Caffeine)**. I implemented a 2-second TTL cache for the /seats endpoint. 
  - *The Trade-off:* Users might see data that is up to 2 seconds stale.
  - *The Benefit:* Even at 10,000 requests per second, the Database is only queried *once every 2 seconds*. The remaining 9,999 requests are served instantly from RAM. If a user clicks a stale (already taken) seat, the Pessimistic Lock in the write-path (Booking) safely rejects them. This is an industry-standard trade-off for high-traffic ticketing systems.