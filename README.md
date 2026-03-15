# BookShop API 📚

A RESTful online bookstore backend built with Spring Boot 3, JWT authentication, MySQL, and Docker.

**Repository**: [Ed-dahmanyRedouane/redouane](https://github.com/Ed-dahmanyRedouane/redouane)

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Tech Stack](#tech-stack)
3. [Project Structure](#project-structure)
4. [Data Model](#data-model)
5. [API Endpoints](#api-endpoints)
6. [Security & Authentication](#security--authentication)
7. [Configuration & Environment Variables](#configuration--environment-variables)
8. [Running Locally (without Docker)](#running-locally-without-docker)
9. [Running with Docker Compose](#running-with-docker-compose)
10. [Pre-seeded Data](#pre-seeded-data)
11. [Testing](#testing)
12. [CI/CD Pipeline](#cicd-pipeline)
13. [Deployment](#deployment)

---

## Project Overview

BookShop API is a stateless RESTful backend for an online bookstore. It provides:

- 📖 **Books catalog** — browsable by category, paginated
- 👥 **User authentication** — JWT-based login with role-based access (ADMIN / USER)
- 🛒 **Shopping cart** — add, update, remove items with stock validation
- 🔐 **Admin operations** — create and delete books
- 📊 **Interactive docs** — Swagger UI available at `/swagger-ui/index.html`
- 🩺 **Health monitoring** — Spring Boot Actuator endpoints

---

## Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 21 LTS |
| Framework | Spring Boot | 3.5.11 |
| Build tool | Maven | 3.9.6 |
| Database (production) | MySQL | 8+ |
| Database (tests) | H2 (in-memory) | — |
| ORM | Spring Data JPA / Hibernate | — |
| Security | Spring Security + JWT (jjwt) | 0.12.5 |
| API documentation | springdoc-openapi (Swagger UI) | 2.7.0 |
| Containerization | Docker + Docker Compose | — |
| CI/CD | GitHub Actions | — |
| Code generation | Lombok | — |
| Password hashing | BCrypt (Spring Security) | — |

---

## Project Structure

```
redouane/
├── .github/
│   └── workflows/
│       └── deploy.yml              # GitHub Actions CI/CD (3 jobs)
├── src/
│   ├── main/
│   │   ├── java/com/bookshop/
│   │   │   ├── BookShopApiApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java         # Spring Security & CORS
│   │   │   │   ├── DataInitializer.java        # Auto-seed users, categories, books
│   │   │   │   ├── GlobalExceptionHandler.java # Centralised error responses
│   │   │   │   └── OpenApiConfig.java          # Swagger/OpenAPI 3 setup
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java         # POST /api/auth/login
│   │   │   │   ├── PublicController.java       # GET /api/public/**
│   │   │   │   ├── CartController.java         # /api/cart/**  (JWT required)
│   │   │   │   └── AdminController.java        # /api/admin/** (ADMIN role)
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest / LoginResponse
│   │   │   │   ├── BookRequest / BookResponse
│   │   │   │   ├── CartItemRequest / CartItemUpdateRequest / CartItemResponse
│   │   │   │   ├── CartResponse
│   │   │   │   └── CategoryResponse
│   │   │   ├── entity/
│   │   │   │   ├── User.java       # Implements UserDetails; roles: USER, ADMIN
│   │   │   │   ├── Category.java
│   │   │   │   ├── Book.java
│   │   │   │   └── CartItem.java   # Unique constraint on (user_id, book_id)
│   │   │   ├── repository/         # Spring Data JPA interfaces
│   │   │   ├── security/
│   │   │   │   ├── JwtUtil.java
│   │   │   │   ├── JwtAuthFilter.java
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   └── service/
│   │   │       ├── AuthService.java
│   │   │       ├── BookService.java
│   │   │       └── CartService.java
│   │   └── resources/
│   │       └── application.yml     # Main configuration
│   └── test/
│       ├── java/com/bookshop/
│       │   ├── BookShopApiApplicationTests.java
│       │   └── controller/         # Integration tests (MockMvc + H2)
│       │       ├── AuthControllerTest.java
│       │       ├── PublicControllerTest.java
│       │       ├── CartControllerTest.java
│       │       └── AdminControllerTest.java
│       └── resources/
│           ├── application.yml
│           └── application-test.yml  # H2 in-memory DB overrides
├── Dockerfile                      # Multi-stage build
├── docker-compose.yml              # App + MySQL orchestration
└── pom.xml
```

---

## Data Model

### Entity Relationships

```
User (1) ────── (N) CartItem (N) ────── (1) Book (N) ────── (1) Category
```

### User

| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK, auto-increment |
| email | String | Unique, not null |
| passwordHash | String | BCrypt encoded |
| role | Enum | `USER` (default) or `ADMIN` |

Implements `UserDetails` for Spring Security integration.

### Category

| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK |
| name | String | Unique, max 100 chars |
| books | List\<Book\> | One-to-many, cascade delete |

### Book

| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK |
| title | String | Not null, max 255 chars |
| author | String | Not null, max 150 chars |
| price | BigDecimal | > 0, precision 10.2 |
| stock | Integer | ≥ 0, default 0 |
| description | String | Optional, TEXT |
| category | Category | Many-to-one (EAGER), not null |

### CartItem

| Field | Type | Notes |
|-------|------|-------|
| id | Long | PK |
| user | User | Many-to-one (LAZY) |
| book | Book | Many-to-one (EAGER) |
| quantity | Integer | ≥ 1 |
| unitPrice | BigDecimal | Snapshot of price at time of add |

Unique constraint on `(user_id, book_id)` — adding the same book again updates the quantity.

---

## API Endpoints

### Auth — `/api/auth`

| Method | Route | Auth | Description | Response |
|--------|-------|------|-------------|----------|
| POST | `/api/auth/login` | Public | Authenticate and receive JWT | `{token, email, role}` |

**Request body:**
```json
{
  "email": "admin@bookshop.com",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "<JWT>",
  "email": "admin@bookshop.com",
  "role": "ADMIN"
}
```

---

### Public — `/api/public` (no JWT required)

| Method | Route | Description | Response |
|--------|-------|-------------|----------|
| GET | `/api/public/categories` | List all categories | `[{id, name}]` |
| GET | `/api/public/books?page=0&size=10` | Paginated book list (sorted by title) | `Page<BookResponse>` |
| GET | `/api/public/books/{id}` | Get a single book | `BookResponse` or 404 |

---

### Cart — `/api/cart` (JWT required)

| Method | Route | Description | Response |
|--------|-------|-------------|----------|
| GET | `/api/cart` | View your cart | `CartResponse` |
| POST | `/api/cart/items` | Add book (upsert) | `CartResponse` (201) |
| PUT | `/api/cart/items/{itemId}` | Update item quantity | `CartResponse` |
| DELETE | `/api/cart/items/{itemId}` | Remove item | 204 No Content |

**Add to cart request:**
```json
{ "bookId": 1, "quantity": 2 }
```

**CartResponse shape:**
```json
{
  "items": [
    {
      "itemId": 1,
      "bookId": 1,
      "title": "Clean Code",
      "quantity": 2,
      "unitPrice": 35.99,
      "subTotal": 71.98
    }
  ],
  "totalItems": 2,
  "totalAmount": 71.98
}
```

Business rules:
- Ownership check: users can only modify their own cart items.
- Adding an existing book to the cart updates its quantity (upsert).
- Adding more than available stock returns HTTP 400.

---

### Admin — `/api/admin` (JWT + ADMIN role required)

| Method | Route | Description | Response |
|--------|-------|-------------|----------|
| POST | `/api/admin/books` | Create a new book | `BookResponse` (201) |
| DELETE | `/api/admin/books/{id}` | Delete a book | 204 No Content |

**Create book request:**
```json
{
  "title": "Design Patterns",
  "author": "Gang of Four",
  "price": 49.99,
  "stock": 20,
  "description": "Classic software design patterns",
  "categoryId": 2
}
```

---

### Monitoring & Docs

| URL | Description |
|-----|-------------|
| `GET /actuator/health` | Health check (used by Docker and CI/CD) |
| `GET /actuator/info` | Application info |
| `GET /swagger-ui/index.html` | Interactive Swagger UI |
| `GET /v3/api-docs` | Raw OpenAPI 3 JSON spec |

---

## Security & Authentication

### Authentication Flow

```
Client  →  POST /api/auth/login  →  AuthService.login()
                                          ↓
                               AuthenticationManager.authenticate()
                               (validates via UserDetailsServiceImpl + BCrypt)
                                          ↓
                               JwtUtil.generateToken()
                                          ↓
Client  ←  { token, email, role }
```

### JWT Token (HS256)

```
Header:  { alg: HS256, typ: JWT }
Payload: {
  sub: "user@bookshop.com",   // email
  role: "USER",               // custom claim
  iat: <issued-at>,
  exp: <expiry>               // default 24 h
}
```

### Request Authorization Flow

Every protected request passes through `JwtAuthFilter`:

1. Extract `Authorization: Bearer <token>` header
2. Validate signature and expiration
3. Load **fresh** user details from the database (ensures role changes take effect immediately)
4. Set the Spring Security context

### Access Control Summary

| Pattern | Access |
|---------|--------|
| `/api/public/**` | Anyone |
| `/api/auth/**` | Anyone |
| `/actuator/**` | Anyone |
| `/swagger-ui/**`, `/v3/api-docs/**` | Anyone |
| `/api/cart/**` | Authenticated users (any role) |
| `/api/admin/**` | ADMIN role only |

CORS is configured to allow all origins, methods, and headers (suitable for development / frontend-agnostic deployments).
Sessions are `STATELESS` and CSRF is disabled (JWT-based API).

---

## Configuration & Environment Variables

### `src/main/resources/application.yml` (defaults)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bookshop_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: 1111
  jpa:
    hibernate:
      ddl-auto: update        # auto-create / update tables

server:
  port: 8080

jwt:
  secret: ${JWT_SECRET:default_dev_secret_change_in_production_must_be_at_least_64_characters_long}
  expiration: ${JWT_EXPIRATION:86400000}   # 24 h in ms

management:
  endpoints.web.exposure.include: health,info
```

### Overridable Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | local MySQL | JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | `root` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | `1111` | DB password |
| `JWT_SECRET` | dev fallback | **Must be ≥ 64 chars in production** |
| `JWT_EXPIRATION` | `86400000` | Token lifetime in milliseconds |
| `SERVER_PORT` | `8080` | HTTP port |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` | Hibernate DDL strategy |

---

## Running Locally (without Docker)

**Prerequisites:** Java 21, Maven 3.9+, MySQL 8+

```bash
# 1. Create the database
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS bookshop_db CHARACTER SET utf8mb4;"

# 2. Clone and build
git clone https://github.com/Ed-dahmanyRedouane/redouane.git
cd redouane
./mvnw clean package -DskipTests

# 3. Run
JWT_SECRET=your-secret-key-at-least-64-characters-long \
SPRING_DATASOURCE_PASSWORD=yourpassword \
java -jar target/bookshop-api-*.jar
```

The API is available at `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

## Running with Docker Compose

```bash
# 1. Clone
git clone https://github.com/Ed-dahmanyRedouane/redouane.git
cd redouane

# 2. Create .env file
cat > .env << 'EOF'
MYSQL_PASSWORD=yourpassword
JWT_SECRET=your-secret-key-at-least-64-characters-long
EOF

# 3. Start
docker compose up -d --build

# 4. Check health
curl http://localhost:8080/actuator/health
```

The compose file starts two services:

| Service | Image | Port |
|---------|-------|------|
| `bookshop-app` | Built from `Dockerfile` | 8080 |
| `mysql` | `mysql:8` | 3306 (internal) |

The app waits for MySQL to be healthy before starting (health-check dependency).

---

## Pre-seeded Data

`DataInitializer` automatically seeds the database on first startup (only if tables are empty):

### Users

| Email | Password | Role |
|-------|----------|------|
| `admin@bookshop.com` | `admin123` | ADMIN |
| `user@bookshop.com` | `user123` | USER |

### Categories

| ID | Name |
|----|------|
| 1 | Fiction |
| 2 | Technology |

### Books

| Title | Author | Price | Stock | Category |
|-------|--------|-------|-------|----------|
| Clean Code | Robert C. Martin | 35.99 | 50 | Technology |
| The Pragmatic Programmer | Hunt & Thomas | 42.50 | 30 | Technology |
| Dune | Frank Herbert | 18.99 | 100 | Fiction |

---

## Testing

### Stack

- **JUnit 5** + **Spring Boot Test** (MockMvc)
- **H2 in-memory database** (active profile: `test`)
- Schema is recreated (`create-drop`) before each test run
- `DataInitializer` seeds the H2 database automatically

### Run Tests

```bash
./mvnw test
```

### Test Coverage

| Test Class | Scenarios |
|------------|-----------|
| `AuthControllerTest` | Login success (admin & user), wrong password (401), missing field (400) |
| `PublicControllerTest` | List categories, paginated books, get book by ID / 404 |
| `CartControllerTest` | View cart (401 without JWT), add book, upsert, stock check (400), update quantity, delete item |
| `AdminControllerTest` | Requires ADMIN role (403 for USER), create book (201), delete book (204) |
| `BookShopApiApplicationTests` | Spring context loads successfully |

A helper `loginAndGetToken(email, password)` method is used across integration tests to obtain a JWT before calling protected endpoints.

---

## CI/CD Pipeline

**File:** `.github/workflows/deploy.yml`

The pipeline has **3 sequential jobs**:

```
build ──► docker ──► deploy
```

| Job | What it does |
|-----|-------------|
| **build** | Checkout → Java 21 → `./mvnw verify` (compile + test) |
| **docker** | Build Docker image → push to registry |
| **deploy** | SSH into server → `docker compose pull && docker compose up -d` |

Triggered on every push to the default branch.

---

## Deployment

The application is deployed on a VPS at `37.27.214.35`.

### Server Setup (first time)

```bash
# Create Linux user
sudo useradd -m -s /bin/bash redouane
sudo passwd redouane
sudo usermod -aG sudo redouane

# Create working directory
sudo mkdir -p /home/redouane/bookshop
sudo chown -R redouane:redouane /home/redouane/bookshop

# Clone repository
cd /home/redouane/bookshop
git clone https://github.com/Ed-dahmanyRedouane/redouane.git .

# Create MySQL database (MySQL pre-installed on server)
mysql -u root -p1111 -e "
  CREATE DATABASE IF NOT EXISTS bookshop_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  GRANT ALL PRIVILEGES ON bookshop_db.* TO 'root'@'172.17.0.1'
    IDENTIFIED BY '1111';
  FLUSH PRIVILEGES;
"

# Create secrets file
cat > .env << 'EOF'
MYSQL_PASSWORD=1111
JWT_SECRET=8f2e9c1b4a7d6e5f8a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f
EOF

# Start
docker compose up -d --build
```

### Verify Deployment

```bash
# Health check
curl http://37.27.214.35:8080/actuator/health

# Login
curl -X POST http://37.27.214.35:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bookshop.com","password":"admin123"}'

# List books (public)
curl "http://37.27.214.35:8080/api/public/books?page=0&size=5"

# Add to cart (replace TOKEN with JWT from login)
curl -X POST http://37.27.214.35:8080/api/cart/items \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"bookId": 1, "quantity": 2}'
```

Swagger UI: `http://37.27.214.35:8080/swagger-ui/index.html`
