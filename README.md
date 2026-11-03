# FIAP PosTech - Tech Challenge Fase 2

Backend desenvolvido em Spring Boot para o sistema de gestão de restaurantes. Cobre autenticação JWT, gerenciamento de usuários, cadastro de restaurantes e cardápios.

## 🛠️ Tecnologias

- Java 21 (Amazon Corretto)
- Spring Boot 3.5
- Spring Security + JWT (JJWT 0.12.5)
- Spring Data JPA + Hibernate
- PostgreSQL 16
- SpringDoc OpenAPI (Swagger UI)
- Docker / Docker Compose

## 📐 Arquitetura

O projeto segue princípios de Clean Architecture com separação por módulos de domínio:

```
com.restaurant
├── auth          → Cadastro e login (RegisterRequest, AuthResponse, AuthService)
├── config        → Configurações gerais (OpenAPI)
├── exception     → Exceções e GlobalExceptionHandler
├── menu          → Itens do cardápio (MenuItem, MenuItemService, MenuItemController)
├── restaurant    → Restaurantes (Restaurant, RestaurantService, RestaurantController)
├── security      → JWT, filtros e configuração Spring Security
├── user          → Usuários, perfil e endereço
└── usertype      → Tipos de usuário (UserType, UserTypeService, UserTypeController)
```

Cada módulo contém: entidade, repositório, serviço, controller e DTOs. Exceções de domínio específicas são tratadas centralizadamente no `GlobalExceptionHandler`.

## 🚀 Como executar

### Com Docker Compose (recomendado)

```bash
docker compose up --build
```

A imagem é multistage — o Maven compila dentro do container, sem necessidade de instalação local.

Acompanhe os logs:

```bash
docker compose logs -f app
```

### Localmente (banco via Docker)

```bash
docker compose up postgres -d
mvn clean package -DskipTests
mvn spring-boot:run
```

## 📋 Endpoints

### Tipos de Usuário

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| POST | /api/v1/user-types | Criar tipo | Sim |
| GET | /api/v1/user-types | Listar todos | Não |
| GET | /api/v1/user-types/{id} | Buscar por ID | Não |
| PUT | /api/v1/user-types/{id} | Atualizar | Sim |
| DELETE | /api/v1/user-types/{id} | Remover | Sim |

### Autenticação

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| POST | /api/v1/auth/register | Cadastro de usuário (OWNER ou CUSTOMER) | Não |
| POST | /api/v1/auth/login | Login | Não |

### Usuários

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| GET | /api/v1/users/me | Buscar perfil | Sim |
| PATCH | /api/v1/users/me | Atualizar dados | Sim |
| PATCH | /api/v1/users/me/password | Alterar senha | Sim |
| DELETE | /api/v1/users/me | Remover conta | Sim |
| GET | /api/v1/users/search?name= | Buscar usuários por nome | Sim |

### Restaurantes

| Método | Endpoint | Descrição | Auth | Role |
|--------|----------|-----------|------|------|
| POST | /api/v1/restaurants | Criar restaurante | Sim | OWNER |
| GET | /api/v1/restaurants | Listar todos (paginado) | Não | — |
| GET | /api/v1/restaurants/{id} | Buscar por ID | Não | — |
| GET | /api/v1/restaurants/search/name?name= | Buscar por nome | Não | — |
| GET | /api/v1/restaurants/search/cuisine?cuisineType= | Buscar por tipo de cozinha | Não | — |
| PUT | /api/v1/restaurants/{id} | Atualizar restaurante | Sim | OWNER (dono) |
| DELETE | /api/v1/restaurants/{id} | Remover restaurante | Sim | OWNER (dono) |

### Cardápio

| Método | Endpoint | Descrição | Auth | Role |
|--------|----------|-----------|------|------|
| POST | /api/v1/restaurants/{id}/menu | Adicionar item | Sim | OWNER (dono) |
| GET | /api/v1/restaurants/{id}/menu | Listar itens (paginado) | Não | — |
| GET | /api/v1/restaurants/{id}/menu/{itemId} | Buscar item por ID | Não | — |
| GET | /api/v1/restaurants/{id}/menu/search?name= | Buscar item por nome | Não | — |
| PUT | /api/v1/restaurants/{id}/menu/{itemId} | Atualizar item | Sim | OWNER (dono) |
| DELETE | /api/v1/restaurants/{id}/menu/{itemId} | Remover item | Sim | OWNER (dono) |

## 📖 Documentação interativa

Com a aplicação rodando:

```
http://localhost:8080/swagger-ui.html
```

## 🧪 Collection de testes

A pasta `collection/restaurant-api/` contém requests no formato Bruno organizados por módulo:

```
collection/restaurant-api/
├── auth/         → requests 01–07
├── users/        → requests 08–16
├── user-types/   → requests 31–39
├── restaurants/  → requests 17–24
├── menu/         → requests 25–30
└── demo/         → roteiro completo para apresentação
```

Para rodar com Bruno CLI:

```bash
npx @usebruno/cli run collection/restaurant-api --env local
```

Para exportar para Postman:

```bash
npx @usebruno/cli export --output postman-collection.json --format postman
```

## 🗄️ Banco de dados

O `init.sql` cria as tabelas na primeira inicialização. O Hibernate gerencia alterações de schema via `ddl-auto=update`.

Tabelas:
- `user_types` — tipos de usuário (OWNER, CUSTOMER e outros)
- `users` — usuários vinculados a um tipo (FK user_types)
- `restaurants` — restaurantes vinculados a um dono (FK users)
- `menu_items` — itens do cardápio vinculados a um restaurante (FK restaurants, CASCADE DELETE)
