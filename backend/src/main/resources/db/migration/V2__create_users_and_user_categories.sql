-- Nguoi dung
CREATE TABLE users (
    id             BIGSERIAL     PRIMARY KEY,
    email          VARCHAR(255)  NOT NULL UNIQUE,
    password_hash  VARCHAR(100)  NOT NULL,
    display_name   VARCHAR(100)  NOT NULL,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- Danh muc: user_id = NULL la danh muc mac dinh dung chung, co user_id la danh muc rieng cua nguoi do.
ALTER TABLE categories ADD COLUMN user_id BIGINT REFERENCES users (id) ON DELETE CASCADE;

-- Ten danh muc chi can khong trung trong pham vi 1 nguoi dung (hoac trong nhom mac dinh).
ALTER TABLE categories DROP CONSTRAINT categories_name_key;
ALTER TABLE categories ADD CONSTRAINT uq_categories_user_name UNIQUE NULLS NOT DISTINCT (user_id, name);

-- Them vai danh muc mac dinh
INSERT INTO categories (name, type, icon) VALUES
    ('Hóa đơn',       'EXPENSE', '🧾'),
    ('Sức khỏe',      'EXPENSE', '💊'),
    ('Học tập',       'EXPENSE', '📚'),
    ('Khác',          'EXPENSE', '📦'),
    ('Thu nhập khác', 'INCOME',  '💵');
