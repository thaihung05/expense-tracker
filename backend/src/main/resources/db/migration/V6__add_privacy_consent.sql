-- Luu bang chung nguoi dung da dong y Chinh sach quyen rieng tu (Luat 91/2025/QH15, Dieu 9:
-- su dong y phai o dinh dang kiem chung duoc). Tai khoan tao truoc ban nay de trong.
ALTER TABLE users ADD COLUMN privacy_accepted_at TIMESTAMPTZ;
ALTER TABLE users ADD COLUMN privacy_version VARCHAR(20);
