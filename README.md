# Concert Ticket Booking Platform

This project is the backend assessment for the Product Backend Engineer position. It provides a robust, concurrency-safe API for customer booking workflows and internal operation workflows.

## 1. How to Setup & Run in Local

### Prerequisites
- Java 21
- Maven
- Docker & Docker Compose

### Running the Application
1. **Start the Database (PostgreSQL):**
   `ash
   docker-compose up -d
   `
2. **Run the Spring Boot Application:**
   `ash
   ./mvnw spring-boot:run
   `
   *Note: Flyway will automatically run database migrations (creating tables and seeding initial data including venues, concerts, and promotional vouchers).*

## 2. API Documents & Testing Collections

### Swagger OpenAPI
Once the application is running, the interactive API documentation is available at:
- **URL:** http://localhost:8080/swagger-ui.html

### Postman Collection
An API testing collection is included in the root directory: postman_collection.json.
- **How to use:** Import this file into Postman.
- **Automation:** The collection is configured with Pre-request scripts. When you call the Login API, it automatically extracts the JWT token and sets it as the {{token}} variable for subsequent requests.

## 3. Coding Guideline & Convention

### Code Structure
The codebase follows a Domain-Driven Monolith architecture, separated by business workflows:
- com.booking_ticket_platform.customer: Endpoints and logic strictly for end-users.
- com.booking_ticket_platform.operation: Endpoints and logic strictly for internal admins/operators.
- com.booking_ticket_platform.shared: Shared configurations, exceptions, and base DTOs.

### How to Code a New API
1. **Controller Layer:** Define the endpoint in the respective workflow package (e.g., CustomerBookingController). Use @PreAuthorize for Role-Based Access Control. Always return data wrapped in ApiResponse<T>.
2. **DTO Layer:** Create specific Request and Response DTOs. Never expose raw Entities to the client.
3. **Service Layer:** Implement business logic here. Apply @Transactional boundaries carefully. If the API modifies critical state (like inventory), use Pessimistic Locks on the Repository level.
4. **Repository Layer:** Use Spring Data JPA. Keep queries optimized.

### How to Write & Run Unit Tests
- **Frameworks:** JUnit 5, Mockito.
- **Command to run tests:**
  `ash
  ./mvnw test
  `
- **Convention:** Write unit tests for business logic (Services) and integration tests for concurrency issues (using ExecutorService and CountDownLatch to simulate multiple threads accessing the same resource). See BookingServiceConcurrencyTest.java for reference.

## 4. System Design & Assumptions
Please refer to the deeply detailed **[system-design.md](./system-design.md)** file. It answers the core requirements:
- How I think about backend design.
- How I reason about possible issues (Overselling, Duplicates, Flash Sale).
- My assumptions, what is IN scope, and what is OUT of scope.