# Hệ thống Đặt vé Sự kiện (Concert Ticket Booking Platform)

Đây là dự án backend phục vụ cho bài Assessment vị trí Product Backend Engineer. Hệ thống cung cấp các RESTful API an toàn, xử lý concurrency tốt, phục vụ cho hai luồng nghiệp vụ chính: Khách hàng (Customer booking workflows) và Vận hành nội bộ (Internal operation workflows).

## 1. Hướng dẫn Cài đặt & Chạy dự án Local

### Yêu cầu môi trường (Prerequisites)
- Java 21
- Maven
- Docker & Docker Compose

### Khởi chạy Ứng dụng
1. **Khởi động Database (PostgreSQL):**
   `ash
   docker-compose up -d
   `
2. **Chạy ứng dụng Spring Boot:**
   `ash
   ./mvnw spring-boot:run
   `
   *Lưu ý: Flyway sẽ tự động chạy các script migrations để tạo bảng và seed dữ liệu ban đầu (bao gồm venues, concerts, và promotional vouchers).*

## 2. Tài liệu API & Bộ kiểm thử (API Documents & Testing)

### Swagger OpenAPI
Sau khi ứng dụng chạy thành công, tài liệu API tương tác trực tiếp có thể truy cập tại:
- **URL:** http://localhost:8080/swagger-ui.html

### Postman Collection
Bộ API testing collection đã được đính kèm ở thư mục gốc của dự án: postman_collection.json.
- **Cách sử dụng:** Import file này vào Postman.
- **Tự động hóa:** Collection đã được setup sẵn các Pre-request scripts. Khi bạn gọi API Login, nó sẽ tự động lấy JWT Token và gán vào biến môi trường {{token}} cho các request tiếp theo.

## 3. Quy chuẩn Code (Coding Guideline & Convention)

### Cấu trúc dự án (Code Structure)
Mã nguồn tuân theo kiến trúc Domain-Driven Monolith, phân tách rạch ròi theo luồng nghiệp vụ:
- com.booking_ticket_platform.customer: Chứa các API và logic dành riêng cho end-user.
- com.booking_ticket_platform.operation: Chứa các API và logic dành riêng cho admin/operator nội bộ.
- com.booking_ticket_platform.shared: Chứa các config, exceptions, và base DTOs dùng chung.

### Hướng dẫn tạo API mới (How to Code a New API)
1. **Controller Layer:** Định nghĩa endpoint tại package nghiệp vụ tương ứng (VD: CustomerBookingController). Sử dụng @PreAuthorize để phân quyền Role-Based Access Control. Luôn trả về dữ liệu được bọc trong class ApiResponse<T>.
2. **DTO Layer:** Tạo các class Request và Response DTO cụ thể. Tuyệt đối không expose raw Entities ra ngoài API.
3. **Service Layer:** Triển khai business logic tại đây. Áp dụng @Transactional để gom nhóm các thao tác. Nếu API có làm thay đổi trạng thái nhạy cảm (như inventory ghế), bắt buộc dùng Pessimistic Locks ở tầng Repository.
4. **Repository Layer:** Sử dụng Spring Data JPA. Giữ các câu query được tối ưu hóa.

### Hướng dẫn viết và chạy Unit Test
- **Frameworks:** JUnit 5, Mockito.
- **Lệnh chạy test:**
  `ash
  ./mvnw test
  `
- **Convention:** Viết unit test cho các business logic (Services) và viết integration test cho các vấn đề concurrency (sử dụng ExecutorService và CountDownLatch để giả lập nhiều threads cùng truy cập vào một resource). Tham khảo file BookingServiceConcurrencyTest.java.

## 4. Thiết kế Hệ thống & Các Giả định
Vui lòng tham khảo tài liệu chi tiết tại **[system-design.md](./system-design.md)**. Tài liệu này sẽ trả lời các yêu cầu cốt lõi của bài test:
- Tư duy thiết kế Backend của tôi.
- Cách tôi giải quyết các vấn đề hóc búa (Overselling, Duplicates, Flash Sale).
- Các giả định, phạm vi những gì ĐÃ LÀM (In scope) và KHÔNG LÀM (Out of scope).