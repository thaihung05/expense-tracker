-- Giao dich thu/chi. Loai thu hay chi lay theo danh muc nen khong luu lai o day.
-- So tien luu bang so nguyen (dong), VND khong co phan le.
CREATE TABLE transactions (
    id           BIGSERIAL     PRIMARY KEY,
    user_id      BIGINT        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    category_id  BIGINT        NOT NULL REFERENCES categories (id),
    amount       BIGINT        NOT NULL CHECK (amount > 0),
    note         VARCHAR(255),
    occurred_on  DATE          NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- Hau het truy van deu loc theo nguoi dung + khoang ngay
CREATE INDEX idx_transactions_user_date ON transactions (user_id, occurred_on DESC);
CREATE INDEX idx_transactions_category ON transactions (category_id);
