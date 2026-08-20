-- Criação da tabela de usuários
-- O Hibernate gerencia o DDL via ddl-auto=update,
-- mas este script garante que o banco e o schema existam
-- antes da aplicação subir pela primeira vez.

CREATE TABLE IF NOT EXISTS users (
    id                   BIGSERIAL PRIMARY KEY,
    name                 VARCHAR(255)        NOT NULL,
    login                VARCHAR(255)        NOT NULL UNIQUE,
    email                VARCHAR(255)        NOT NULL UNIQUE,
    password             VARCHAR(255)        NOT NULL,
    role                 VARCHAR(20)         NOT NULL CHECK (role IN ('OWNER', 'CUSTOMER')),
    street               VARCHAR(255)        NOT NULL,
    number               VARCHAR(20)         NOT NULL,
    complement           VARCHAR(255),
    city                 VARCHAR(255)        NOT NULL,
    state                VARCHAR(2)          NOT NULL,
    zip_code             VARCHAR(9)          NOT NULL,
    created_at           TIMESTAMP           NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP           NOT NULL DEFAULT now(),
    password_changed_at  TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_login ON users (login);
CREATE INDEX IF NOT EXISTS idx_users_name  ON users (name);
