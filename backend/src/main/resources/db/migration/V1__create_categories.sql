CREATE TABLE categories (
    id      BIGSERIAL    PRIMARY KEY,
    name    VARCHAR(50)  NOT NULL UNIQUE,
    type    VARCHAR(10)  NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    icon    VARCHAR(10)
);

INSERT INTO categories (name, type, icon) VALUES
    ('Ăn uống',  'EXPENSE', '🍜'),
    ('Đi lại',   'EXPENSE', '🛵'),
    ('Nhà cửa',  'EXPENSE', '🏠'),
    ('Mua sắm',  'EXPENSE', '🛍️'),
    ('Giải trí', 'EXPENSE', '🎮'),
    ('Lương',    'INCOME',  '💰'),
    ('Thưởng',   'INCOME',  '🎁');