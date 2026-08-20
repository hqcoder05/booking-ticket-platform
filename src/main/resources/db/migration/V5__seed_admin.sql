INSERT INTO users (id, email, password_hash, role)
VALUES ('33333333-3333-3333-3333-333333333333', 'admin@geekup.vn', '$2a$10$wN9a4c5X9nC.lU.hG1vA7.JzB0l9E4K/gXw.v6sHqC1M.j.qZJ73O', 'ADMIN')
ON CONFLICT (email) DO NOTHING;