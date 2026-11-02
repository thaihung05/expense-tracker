-- Tu khoa app tu hoc: nguoi dung ghi "tra sua" vao danh muc An uong
-- thi lan sau go "tra sua 40k" app tu chon dung danh muc.
-- keyword luu dang chu thuong, bo dau (vd: "tra sua").
CREATE TABLE keyword_rules (
    id           BIGSERIAL     PRIMARY KEY,
    user_id      BIGINT        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    keyword      VARCHAR(60)   NOT NULL,
    category_id  BIGINT        NOT NULL REFERENCES categories (id) ON DELETE CASCADE,
    hits         INT           NOT NULL DEFAULT 1,
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_keyword_rules_user_keyword UNIQUE (user_id, keyword)
);
