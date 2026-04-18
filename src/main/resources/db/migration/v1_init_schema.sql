-- liquibase formatted sql

-- changeset developer:1
-- preconditions onFail:MARK_RAN onError:HALT
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'users'
CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(50)  NOT NULL,
    deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);

-- changeset developer:2
-- preconditions onFail:MARK_RAN onError:HALT
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'cards'
CREATE TABLE cards
(
    id              BIGSERIAL PRIMARY KEY,
    card_number     VARCHAR(16)    NOT NULL UNIQUE,
    user_id         BIGINT         NOT NULL,
    expiration_date DATE           NOT NULL,
    status          VARCHAR(50)    NOT NULL,
    balance         DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    created_at      TIMESTAMP      NOT NULL,
    updated_at      TIMESTAMP      NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id)
);