-- =============================================
-- V4: Seed tất cả dữ liệu mẫu cho hệ thống
-- Bao gồm: Users, Venues, Concerts, Ticket Categories, Seats, Vouchers
-- =============================================

-- 1. USERS (password cho tất cả tài khoản: admin123)
-- BCrypt hash của 'admin123'
INSERT INTO users (id, email, password_hash, role) VALUES
    ('a0a0a0a0-a0a0-a0a0-a0a0-a0a0a0a0a0a0', 'admin@geekup.vn', '$2a$10$9Uvbi3tZpI3C84JnKE5k6egJe.2YKFuR6ghpwJHlywl4A53lJLoMG', 'ADMIN'),
    ('b0b0b0b0-b0b0-b0b0-b0b0-b0b0b0b0b0b0', 'operator@geekup.vn', '$2a$10$9Uvbi3tZpI3C84JnKE5k6egJe.2YKFuR6ghpwJHlywl4A53lJLoMG', 'OPERATOR'),
    ('c0c0c0c0-c0c0-c0c0-c0c0-c0c0c0c0c0c0', 'customer@geekup.vn', '$2a$10$9Uvbi3tZpI3C84JnKE5k6egJe.2YKFuR6ghpwJHlywl4A53lJLoMG', 'CUSTOMER')
ON CONFLICT (email) DO NOTHING;

-- 2. VENUES (Địa điểm tổ chức)
INSERT INTO venues (id, name, address, city, capacity) VALUES
    ('10000000-0000-0000-0000-000000000001', 'Sân vận động Quốc gia Mỹ Đình', 'Đường Lê Đức Thọ, Nam Từ Liêm', 'Hà Nội', 40000),
    ('10000000-0000-0000-0000-000000000002', 'Nhà hát Hòa Bình', '240 Đường 3/2, Quận 10', 'TP. Hồ Chí Minh', 2500),
    ('10000000-0000-0000-0000-000000000003', 'Phú Thọ Indoor Stadium', '1 Lữ Gia, Quận 11', 'TP. Hồ Chí Minh', 5000)
ON CONFLICT DO NOTHING;

-- 3. CONCERTS (Sự kiện)
INSERT INTO concerts (id, venue_id, name, event_date, status, stage_layout) VALUES
    ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
     'FC Bayern Munich x BTS - World Tour 2027', '2027-03-15 19:00:00', 'PUBLISHED', 'CENTER'),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000003',
     'BLACKPINK - Born Pink World Tour Finale', '2027-04-20 20:00:00', 'PUBLISHED', 'FRONT'),
    ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002',
     'Sơn Tùng M-TP - Sky Tour 2027', '2027-06-10 19:30:00', 'DRAFT', 'FRONT')
ON CONFLICT DO NOTHING;

-- 4. TICKET CATEGORIES (Hạng vé)
-- Concert 1: FC Bayern Munich x BTS (Sân vận động Mỹ Đình - 40.000 chỗ)
INSERT INTO ticket_categories (id, concert_id, name, type, price, total_quantity, available_quantity, version) VALUES
    ('30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001',
     'VVIP - Sân khấu gần nhất', 'SEATED', 5000000.00, 100, 100, 0),
    ('30000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001',
     'VIP - Khán đài A', 'SEATED', 3000000.00, 200, 200, 0),
    ('30000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000001',
     'Standard - Khán đài B', 'SEATED', 1500000.00, 500, 500, 0),
    ('30000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001',
     'Standing - Sân cỏ', 'STANDING', 800000.00, 5000, 5000, 0)
ON CONFLICT DO NOTHING;

-- Concert 2: BLACKPINK (Phú Thọ - 5.000 chỗ)
INSERT INTO ticket_categories (id, concert_id, name, type, price, total_quantity, available_quantity, version) VALUES
    ('30000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000002',
     'VIP - Hàng đầu', 'SEATED', 4000000.00, 100, 100, 0),
    ('30000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000002',
     'Standard - Khu A', 'SEATED', 2000000.00, 300, 300, 0),
    ('30000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000002',
     'Economy - Khu B', 'SEATED', 1000000.00, 600, 600, 0)
ON CONFLICT DO NOTHING;

-- Concert 3: Sơn Tùng M-TP (Nhà hát Hòa Bình - 2.500 chỗ)
INSERT INTO ticket_categories (id, concert_id, name, type, price, total_quantity, available_quantity, version) VALUES
    ('30000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000003',
     'VIP', 'SEATED', 2500000.00, 200, 200, 0),
    ('30000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000003',
     'Standard', 'SEATED', 1200000.00, 800, 800, 0)
ON CONFLICT DO NOTHING;

-- 5. SEATS (Ghế ngồi cho hạng SEATED)
-- Concert 1 - VVIP (100 ghế)
INSERT INTO seats (id, ticket_category_id, seat_number, status, version)
SELECT uuid_generate_v4(), '30000000-0000-0000-0000-000000000001', 'VVIP-A' || n, 'AVAILABLE', 0
FROM generate_series(1, 100) AS n;

-- Concert 1 - VIP (200 ghế)
INSERT INTO seats (id, ticket_category_id, seat_number, status, version)
SELECT uuid_generate_v4(), '30000000-0000-0000-0000-000000000002', 'VIP-B' || n, 'AVAILABLE', 0
FROM generate_series(1, 200) AS n;

-- Concert 1 - Standard (500 ghế)
INSERT INTO seats (id, ticket_category_id, seat_number, status, version)
SELECT uuid_generate_v4(), '30000000-0000-0000-0000-000000000003', 'STD-C' || n, 'AVAILABLE', 0
FROM generate_series(1, 500) AS n;

-- Concert 2 - VIP (100 ghế)
INSERT INTO seats (id, ticket_category_id, seat_number, status, version)
SELECT uuid_generate_v4(), '30000000-0000-0000-0000-000000000005', 'VIP-' || n, 'AVAILABLE', 0
FROM generate_series(1, 100) AS n;

-- Concert 2 - Standard (300 ghế)
INSERT INTO seats (id, ticket_category_id, seat_number, status, version)
SELECT uuid_generate_v4(), '30000000-0000-0000-0000-000000000006', 'STD-' || n, 'AVAILABLE', 0
FROM generate_series(1, 300) AS n;

-- Concert 2 - Economy (600 ghế)
INSERT INTO seats (id, ticket_category_id, seat_number, status, version)
SELECT uuid_generate_v4(), '30000000-0000-0000-0000-000000000007', 'ECO-' || n, 'AVAILABLE', 0
FROM generate_series(1, 600) AS n;

-- Concert 3 - VIP (200 ghế)
INSERT INTO seats (id, ticket_category_id, seat_number, status, version)
SELECT uuid_generate_v4(), '30000000-0000-0000-0000-000000000008', 'VIP-' || n, 'AVAILABLE', 0
FROM generate_series(1, 200) AS n;

-- Concert 3 - Standard (800 ghế)
INSERT INTO seats (id, ticket_category_id, seat_number, status, version)
SELECT uuid_generate_v4(), '30000000-0000-0000-0000-000000000009', 'STD-' || n, 'AVAILABLE', 0
FROM generate_series(1, 800) AS n;

-- 6. VOUCHERS (Mã giảm giá)
INSERT INTO vouchers (id, code, discount_type, discount_value, max_usage, current_usage) VALUES
    ('d0000000-0000-0000-0000-000000000001', 'EARLYBIRD', 'PERCENTAGE', 10, 100, 0),
    ('d0000000-0000-0000-0000-000000000002', 'FLASHSALE50K', 'FIXED_AMOUNT', 50000, 200, 0),
    ('d0000000-0000-0000-0000-000000000003', 'VIP20', 'PERCENTAGE', 20, 50, 0),
    ('d0000000-0000-0000-0000-000000000004', 'GEEKUP100K', 'FIXED_AMOUNT', 100000, 30, 0)
ON CONFLICT DO NOTHING;
