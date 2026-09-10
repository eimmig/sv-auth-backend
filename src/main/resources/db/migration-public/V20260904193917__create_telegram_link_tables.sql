CREATE TABLE telegram_link (
    telegram_user_id  VARCHAR(255) PRIMARY KEY,
    tenant_id         VARCHAR(56) NOT NULL,
    user_id           UUID NOT NULL
);

CREATE TABLE pending_telegram_link (
    code        VARCHAR(16) PRIMARY KEY,
    tenant_id   VARCHAR(56) NOT NULL,
    user_id     UUID NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL
);
