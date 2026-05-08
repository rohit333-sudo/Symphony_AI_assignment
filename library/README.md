# Library Management System API

A Spring Boot REST API for a small library system. Manage authors, books, members, and borrow records with full business rule enforcement.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.0.6 |
| Language | Java 21 |
| Database | H2 (in-memory) |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| Build | Maven |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Tests | JUnit 5 + Mockito |

---

## Prerequisites

- Java 21+
- Maven 3.8+

No external database setup required — H2 runs entirely in memory.

---

## Setup & Run

### 1. Clone the project
```bash
git clone https://github.com/your-repo/library-api.git
cd library-api
```

### 2. Build
```bash
mvn clean install
```

### 3. Run
```bash
mvn spring-boot:run
```

The server starts on **http://localhost:8080**

---

## Useful URLs

| URL | Description |
|---|---|
| http://localhost:8080/api/info | App name, version, loan config |
| http://localhost:8080/swagger-ui/index.html | Swagger UI — interactive API docs |
| http://localhost:8080/v3/api-docs | Raw OpenAPI JSON |



## Configuration

All values are in `src/main/resources/application.properties`:

```properties
app.name=Library Management System
app.version=1.0.0
library.loan-period-days=14
library.max-active-borrows-per-member=3
```

Change `loan-period-days` or `max-active-borrows-per-member` without touching any Java code.

---

## Running Tests

```bash
mvn test
```

Service layer tests cover:
- `AuthorServiceTest` — create, get, update, duplicate email
- `BookServiceTest` — create, filter, delete, duplicate ISBN, borrowed-book guard
- `BorrowServiceTest` — borrow success, book not available, max limit, return, overdue marking

---

## API Endpoints

### Info
```
GET  /api/info
```

### Authors
```
POST   /api/authors
GET    /api/authors
GET    /api/authors/{id}
PUT    /api/authors/{id}
```

### Books
```
POST   /api/books
GET    /api/books
GET    /api/books?available=true
GET    /api/books/{id}
PUT    /api/books/{id}
DELETE /api/books/{id}
```

### Members
```
POST   /api/members
GET    /api/members
GET    /api/members/{id}
PUT    /api/members/{id}
GET    /api/members/{id}/borrows
```

### Borrows
```
POST   /api/borrows
PUT    /api/borrows/{id}/return
```

---

## Sample curl Requests

### Get app info
```bash
curl http://localhost:8080/api/info
```

### Create an author
```bash
curl -X POST http://localhost:8080/api/authors \
  -H "Content-Type: application/json" \
  -d '{"name": "George Orwell", "email": "orwell@books.com"}'
```

### Get all authors
```bash
curl http://localhost:8080/api/authors
```

### Get author by ID
```bash
curl http://localhost:8080/api/authors/1
```

### Update an author
```bash
curl -X PUT http://localhost:8080/api/authors/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Eric Blair", "email": "blair@books.com"}'
```

### Add a book
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title": "1984", "isbn": "978-0451524935", "authorId": 1}'
```

### List all books
```bash
curl http://localhost:8080/api/books
```

### List only available books
```bash
curl "http://localhost:8080/api/books?available=true"
```

### Get book by ID
```bash
curl http://localhost:8080/api/books/1
```

### Update a book
```bash
curl -X PUT http://localhost:8080/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Nineteen Eighty-Four", "isbn": "978-0451524935", "authorId": 1}'
```

### Delete a book
```bash
curl -X DELETE http://localhost:8080/api/books/1
```

### Register a member
```bash
curl -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice Smith", "email": "alice@library.com", "phone": "+91-9876543210"}'
```

### Get all members
```bash
curl http://localhost:8080/api/members
```

### Get member by ID
```bash
curl http://localhost:8080/api/members/1
```

### Update a member
```bash
curl -X PUT http://localhost:8080/api/members/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice Johnson", "email": "alice@library.com"}'
```

### Borrow a book
```bash
curl -X POST http://localhost:8080/api/borrows \
  -H "Content-Type: application/json" \
  -d '{"bookId": 1, "memberId": 1}'
```

### Return a book
```bash
curl -X PUT http://localhost:8080/api/borrows/1/return
```

### Get borrow history for a member
```bash
curl http://localhost:8080/api/members/1/borrows
```

---

## Business Rules

| Rule | Behaviour |
|---|---|
| Book already borrowed | `POST /api/borrows` returns `422` with message |
| Member at borrow limit | `POST /api/borrows` returns `422` with message |
| Delete borrowed book | `DELETE /api/books/{id}` returns `400` |
| Return already-returned | `PUT /api/borrows/{id}/return` returns `400` |
| Duplicate email/ISBN | Returns `409 Conflict` |
| Invalid request fields | Returns `400` with per-field `validationErrors` map |

---

## Project Structure

```
src/
├── main/
│   ├── java/com/org/library/
│   │   ├── LibraryApplication.java
│   │   ├── config/
│   │   │   ├── AppProperties.java
│   │   │   ├── LibraryProperties.java
│   │   │   └── OpenApiConfig.java
│   │   ├── controller/
│   │   │   ├── AuthorController.java
│   │   │   ├── BookController.java
│   │   │   ├── BorrowController.java
│   │   │   ├── InfoController.java
│   │   │   └── MemberController.java
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── entity/
│   │   ├── enums/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   └── resources/
│       └── application.properties
├── test/
│   └── java/com/org/library/service/
│       ├── AuthorServiceTest.java
│       ├── BookServiceTest.java
│       └── BorrowServiceTest.java
└── openapi.yaml
```
