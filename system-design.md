# Tài liệu Thiết kế Hệ thống & Cơ sở dữ liệu

Tài liệu này trình bày các quyết định về kiến trúc, sự đánh đổi (trade-offs), và tư duy kỹ thuật đằng sau hệ thống Concert Ticket Booking Platform.

## 1. Tư duy Thiết kế Backend (How I Think About Backend Design)

Cách tiếp cận của tôi đối với hệ thống này đặt **Tính toàn vẹn dữ liệu (Data Integrity), Sự tối giản (Simplicity), và Khả năng bảo vệ thiết kế (Defensibility)** lên hàng đầu thay vì tối ưu hóa viển vông (premature optimization). Với ngữ cảnh giới hạn 48 giờ và startup chuẩn bị chạy Flash Sale, triết lý của tôi là:
- **Monolith First:** Một kiến trúc Monolith được module hóa rõ ràng (phân tách rạch ròi domain customer và operation) mang lại giá trị cao hơn nhiều so với một kiến trúc Microservices làm vội vàng. Nó loại bỏ độ trễ mạng (network latency) giữa các services và giúp việc quản lý ACID transactions trở nên an toàn tuyệt đối.
- **Let the Database Do the Heavy Lifting:** PostgreSQL là một hệ quản trị CSDL cực kỳ mạnh mẽ. Thay vì nhồi nhét các cơ chế distributed locks từ bên ngoài (như Redis/Zookeeper) làm tăng độ phức tạp vận hành, tôi tận dụng triệt để khóa mức dòng (Row-level locking: SELECT FOR UPDATE) của PostgreSQL để đảm bảo tính nhất quán dữ liệu.

## 2. Thiết kế Cơ sở dữ liệu (Database Design)

Database được chuẩn hóa (normalized) hoàn toàn và quản lý version qua **Flyway Migrations**.
- **Sử dụng UUID làm Primary Keys:** Chống lại các rủi ro bảo mật (ID enumeration attacks) và dọn đường sẵn cho việc phân tán dữ liệu (horizontal partitioning) trong tương lai.
- **Core Entities:** User, Concert, Venue, Seat (Inventory), Booking, Payment, Voucher.
- **Hỗ trợ Concurrency:** Các bảng Seat và Voucher được xác định là "nút thắt cổ chai" (bottlenecks) trong đợt flash sale. Chúng được thiết kế để có thể khóa (lock) an toàn trong các transactions.

## 3. Giới hạn Phạm vi & Các Giả định (Assumptions & Scope)

Để mang lại giá trị cốt lõi nhất trong thời gian giới hạn, tôi đã khoanh vùng phạm vi dự án một cách cẩn thận và đưa ra các giả định kinh doanh (business assumptions) sau:

### Những tính năng ĐÃ LÀM (In Scope):
- **Máy trạng thái 4-bước cho Booking:** Vòng đời của một đơn hàng tuân thủ nghiêm ngặt 4 states: PENDING -> PAID -> CANCELLED (nếu quá 5 phút không thanh toán hoặc bị hủy) -> REFUNDED (nếu operation hủy show).
- **Idempotent API:** Hỗ trợ safe retries (gọi lại API mà không sợ lỗi duplicate) cho luồng Booking.
- **Seat Auto-Release (Nhả ghế tự động):** Có một Scheduled Job chạy ngầm mỗi 5 giây để quét và nhả các ghế PENDING về lại trạng thái AVAILABLE nếu khách hàng không thanh toán trong vòng 5 phút.
- **Anti-Seat Hoarding (Chống DoS/Đầu cơ ghế):** Một luật kinh doanh khắt khe: 1 User chỉ được phép có tối đa 1 booking ở trạng thái PENDING cùng lúc. Điều này ngăn chặn việc kẻ gian dùng tool khóa toàn bộ ghế của rạp.

### Những tính năng CHƯA LÀM (Out of Scope / Limitations):
- **CRUD Operations cho Vouchers:** Đúng như gợi ý từ yêu cầu đề bài, hệ thống *KHÔNG* cung cấp các API cho màn hình Operation để create/update/delete vouchers. Thay vào đó, dữ liệu voucher được seed trực tiếp vào database qua script của Flyway. Trọng tâm của hệ thống là làm sao để hàng ngàn khách hàng có thể áp dụng voucher đó cùng lúc (apply) mà không bị lỗi concurrency.
- **Tích hợp Cổng thanh toán thật (Payment Gateway):** API /pay hiện tại được mock quá trình thanh toán thành công. Việc này giúp tôi dồn toàn lực vào bài toán chuyển đổi state (Order State Transitions) thay vì mất thời gian tích hợp SDK của bên thứ 3.
- **WebSockets cho Bản đồ ghế:** Thay vì cấu hình WebSockets phức tạp, hệ thống giả định rằng Frontend sẽ sử dụng cơ chế Polling (Ví dụ: React Query refetch API bản đồ ghế mỗi 5 giây).

## 4. Giải quyết các Vấn đề & Rủi ro Cốt lõi (Issues and Workflows)

Startup bày tỏ sự lo ngại đặc biệt về đợt Flash Sale (50.000 users, 300-500 bookings/min). Dưới đây là cách hệ thống nhận diện và giải quyết các rủi ro này:

### A. Bán lố vé (Overselling Tickets / Inventory Contention)
- **Rủi ro:** Hàng ngàn user cùng click chọn một chiếc ghế VIP ở cùng một phần ngàn giây.
- **Giải pháp:** Pessimistic Locking. Hệ thống sử dụng @Lock(LockModeType.PESSIMISTIC_WRITE) khi truy vấn Seat. Transaction đầu tiên lọt vào sẽ giữ khóa (lock) Database; các transactions đến sau bắt buộc phải chờ. Ngay khi transaction đầu tiên commit, ghế sẽ chuyển sang PENDING, và các transaction đến sau sẽ ngay lập tức bị báo lỗi (chặn đứng từ vòng gửi xe) chứ không gây ra lỗi double-booking.

### B. Sinh ra 2 vé do lỗi mạng (Duplicate Bookings Caused by Retries)
- **Rủi ro:** Mạng của user bị lag, họ bấm "Đặt vé" 2 lần liên tiếp, dẫn tới việc trừ tiền 2 lần và sinh ra 2 booking.
- **Giải pháp:** Hệ thống yêu cầu Client phải sinh ra một Idempotency-Key (UUID) và nhét vào HTTP Header cho mỗi luồng đặt vé. Bảng ookings có cấu hình UNIQUE constraint cho cột này. Nếu có concurrent retry, Database sẽ ném ra lỗi DataIntegrityViolationException, và Exception Handler của Spring sẽ bắt lại, từ chối request thứ hai một cách an toàn.

### C. Gian lận Voucher (Users Abusing Promotional Vouchers)
- **Rủi ro:** Một voucher giới hạn max_usage = 100 lượt. Nhưng có 10.000 user cùng apply mã đó đồng thời.
- **Giải pháp:** Atomic updates (Cập nhật nguyên tử). Voucher được gọi ra kèm theo Pessimistic Write Lock (indByCodeForUpdate). Application sẽ kiểm tra logic current_usage < max_usage, sau đó cộng current_usage lên 1, và lưu xuống DB trong cùng một transaction boundary cực kỳ khép kín. (Đã được chứng minh bằng code thực tế qua file test VoucherConcurrencyTest.java).

### D. Hệ thống quá tải khi Flash Sale (System Instability)
- **Rủi ro:** 50.000 users cùng bấm F5/Polling liên tục để ngắm bản đồ ghế trống. Việc này sẽ khiến CPU của Database chạy quá tải 100% và sập hệ thống (System Crash).
- **Giải pháp:** Ứng dụng **Short-lived In-Memory Cache (Caffeine)**. Tôi đã tích hợp Caffeine Cache với TTL (Time-to-live) cực ngắn là 2 giây cho API lấy danh sách ghế (/seats).
  - *Sự đánh đổi (Trade-off):* Người dùng có thể nhìn thấy dữ liệu cũ (stale data) bị trễ tối đa 2 giây.
  - *Lợi ích khổng lồ:* Dù có 10.000 requests/giây gọi vào hệ thống, Database chỉ bị query đúng *1 lần duy nhất mỗi 2 giây*. 9.999 requests còn lại được phục vụ tốc độ bàn thờ ngay trên RAM (Caffeine). Nếu user vô tình click vào cái ghế đã bị mua trong 2 giây trễ đó, thì cơ chế Pessimistic Lock ở luồng Write bên trên sẽ bảo vệ và chặn họ lại. Đây là tiêu chuẩn thiết kế High-Concurrency của các nền tảng bán vé lớn hiện nay.