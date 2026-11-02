-- Ngan sach: han muc chi moi thang cho tung danh muc (ap dung lap lai moi thang).
CREATE TABLE budgets (
    id            BIGSERIAL  PRIMARY KEY,
    user_id       BIGINT     NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    category_id   BIGINT     NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    amount_limit  BIGINT     NOT NULL CHECK (amount_limit > 0),
    CONSTRAINT uq_budgets_user_category UNIQUE (user_id, category_id)
);
