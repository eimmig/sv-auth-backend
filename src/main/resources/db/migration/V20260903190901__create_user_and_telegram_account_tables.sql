CREATE TABLE users (
    id                    UUID PRIMARY KEY,
    name                  VARCHAR(255) NOT NULL,
    email                 VARCHAR(255) NOT NULL,
    password_hash         VARCHAR(255) NOT NULL,
    role                  VARCHAR(20) NOT NULL CHECK (role IN ('admin', 'member')),
    must_change_password  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE telegram_accounts (
    id                UUID PRIMARY KEY,
    user_id           UUID NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    telegram_user_id  VARCHAR(255) NOT NULL,
    linked_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_telegram_accounts_telegram_user_id UNIQUE (telegram_user_id)
);
