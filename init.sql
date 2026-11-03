-- Criação da tabela de usuários
-- O Hibernate gerencia o DDL via ddl-auto=update,
-- mas este script garante que o banco e o schema existam
-- antes da aplicação subir pela primeira vez.

-- Tabela de tipos de usuário
CREATE TABLE IF NOT EXISTS user_types (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Tipos padrão (OWNER e CUSTOMER)
INSERT INTO user_types (name) VALUES ('OWNER')    ON CONFLICT DO NOTHING;
INSERT INTO user_types (name) VALUES ('CUSTOMER') ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS users (
    id                   BIGSERIAL PRIMARY KEY,
    name                 VARCHAR(255)        NOT NULL,
    login                VARCHAR(255)        NOT NULL UNIQUE,
    email                VARCHAR(255)        NOT NULL UNIQUE,
    password             VARCHAR(255)        NOT NULL,
    role                 VARCHAR(20)         NOT NULL CHECK (role IN ('OWNER', 'CUSTOMER')),
    user_type_id         BIGINT              NOT NULL REFERENCES user_types (id),
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

-- Tabela de restaurantes
CREATE TABLE IF NOT EXISTS restaurants (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(255)  NOT NULL,
    street         VARCHAR(255)  NOT NULL,
    number         VARCHAR(20)   NOT NULL,
    complement     VARCHAR(255),
    city           VARCHAR(255)  NOT NULL,
    state          VARCHAR(2)    NOT NULL,
    zip_code       VARCHAR(9)    NOT NULL,
    cuisine_type   VARCHAR(100)  NOT NULL,
    opening_time   TIME          NOT NULL,
    closing_time   TIME          NOT NULL,
    owner_id       BIGINT        NOT NULL REFERENCES users (id),
    created_at     TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_restaurants_name         ON restaurants (name);
CREATE INDEX IF NOT EXISTS idx_restaurants_cuisine_type ON restaurants (cuisine_type);
CREATE INDEX IF NOT EXISTS idx_restaurants_owner_id     ON restaurants (owner_id);

-- Tabela de itens do cardápio
CREATE TABLE IF NOT EXISTS menu_items (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255)     NOT NULL,
    description   TEXT,
    price         NUMERIC(10, 2)   NOT NULL,
    dine_in_only  BOOLEAN          NOT NULL DEFAULT false,
    photo_path    VARCHAR(500),
    restaurant_id BIGINT           NOT NULL REFERENCES restaurants (id) ON DELETE CASCADE,
    created_at    TIMESTAMP        NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP        NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_menu_items_restaurant_id ON menu_items (restaurant_id);
CREATE INDEX IF NOT EXISTS idx_menu_items_name          ON menu_items (name);
