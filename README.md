# FIAP PosTech - Tech Challenge Fase 1

Backend desenvolvido em Spring Boot para o sistema de gestão de restaurantes. Cobre cadastro e autenticação de usuários, controle de acesso via JWT, atualização de perfil, troca de senha e busca paginada de usuários.

## 🛠️ Tecnologias

- Java 21 (Amazon Corretto)
- Spring Boot 3.5
- Spring Security + JWT
- PostgreSQL 16
- Docker + Docker Compose
- Maven

## 🚀 Como executar

### 1. Subir com Docker Compose (recomendado)

```bash
docker compose up --build
```

A imagem é construída em multistage — o Maven roda dentro do container, sem necessidade de instalação local.

Aguarde o banco ficar saudável e a aplicação iniciar. Acompanhe os logs com:

```bash
docker compose logs -f app
```

### 2. Rodar localmente (banco via Docker)

Sobe apenas o banco:

```bash
docker compose up postgres -d
```

Gera o pacote e executa:

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

## 📋 Endpoints principais

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| POST | /api/v1/auth/register | Cadastro de usuário | Não |
| POST | /api/v1/auth/login | Login | Não |
| GET | /api/v1/users/me | Buscar perfil | Sim |
| PATCH | /api/v1/users/me | Atualizar dados | Sim |
| PATCH | /api/v1/users/me/password | Alterar senha | Sim |
| DELETE | /api/v1/users/me | Remover conta | Sim |
| GET | /api/v1/users/search?name= | Buscar por nome | Sim |

## 📖 Documentação

Com a aplicação rodando, acesse:

```
http://localhost:8080/swagger-ui.html
```

## 🧪 Collection de testes

A pasta `collection/restaurant-api/` contém os requests no formato Bruno, cobrindo todos os cenários obrigatórios da especificação.

Para importar no Postman:

```bash
npx @usebruno/cli export --output postman-collection.json --format postman
```

## 🔑 Variáveis de ambiente

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| SPRING_DATASOURCE_URL | jdbc:postgresql://postgres:5432/restaurant | URL do banco |
| SPRING_DATASOURCE_USERNAME | admin | Usuário do banco |
| SPRING_DATASOURCE_PASSWORD | admin123 | Senha do banco |
| JWT_SECRET | (valor padrão) | Chave de assinatura JWT — troque em produção |
