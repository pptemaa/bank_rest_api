-- liquibase formatted sql

-- changeset developer:1
CREATE TABLE users
(
    id       BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(50)  NOT NULL
);

-- changeset developer:2
CREATE TABLE cards
(
    id              BIGSERIAL PRIMARY KEY,
    card_number     VARCHAR(16)    NOT NULL UNIQUE,
    user_id         BIGINT         NOT NULL,
    expiration_date DATE           NOT NULL,
    status          VARCHAR(50)    NOT NULL,
    balance         DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id)
);