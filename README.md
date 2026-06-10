# Hexagonal Banking API with Spring Modulith & Kafka

A robust, scalable banking REST API built with Spring Boot, demonstrating strict **Domain-Driven Design (DDD)**, **Hexagonal Architecture (Ports and Adapters)**, and **Spring Modulith**.

This project goes beyond standard MVC to enforce strict business rules, isolated domain logic, high-performance database interactions, and resilient event-driven communication.

## 🏗 Architecture Overview

This application adheres to Hexagonal Architecture and uses **Spring Modulith** to logically separate bounded contexts. Each business module (e.g., `account`, `transaction`, `user`) is self-contained and enforces strict architectural boundaries.

### Module Internal Structure

Within each module, the codebase is divided into three primary layers:

### 1. Domain Layer (`domain`)

The heart of the application. It contains pure Java models (Account, User, TransactionRecord).

* **Zero Dependencies:** No Spring annotations, no JPA, no web concepts.
* **Business Invariants:** Entities strictly protect their own state (e.g., `Account.withdraw()` throws an exception if funds are insufficient).

### 2. Application Layer (`application`)

The orchestrator. It defines the "Ports" (interfaces) that allow the outside world to interact with the Domain.

* **Inbound Ports (Use Cases):** Interfaces defining what the system can do (e.g., `DepositMoneyUseCase`).
* **Outbound Ports (SPIs):** Interfaces defining what the system needs from the outside world (e.g., `AccountRepository`).

### 3. Infrastructure Layer (`infrastructure/adapter`)

The outer shell. It adapts external technologies to the core application's ports.

* **Inbound Adapters:** REST Controllers that translate HTTP requests into Use Case Commands, or Kafka Listeners that consume external events.
* **Outbound Adapters:** Spring Data JPA repositories translating pure Domain objects into database rows, or Outbox Event publishers for messaging.

## ✨ Key Design Decisions

* **Transactional Outbox Pattern & Kafka:** To guarantee data consistency, domain events are saved to an outbox table in the same local transaction as the business entity. A background scheduler publishes these to **Apache Kafka**, where they are processed idempotently.
* **Strict Security Contract:** Authentication and authorization are enforced via **JWT (JSON Web Tokens)** passed in the `Authorization` header. The core domain verifies account ownership before executing any mutations.
* **JPA Optimization:** JPA entities implement `Persistable`. The Outbound Port is explicitly split into `insert()` and `update()`, allowing the adapter to control the `isNew` flag, eliminating Hibernate's unnecessary `SELECT` before `INSERT`.
* **Modularity Verification:** Spring Modulith's test support ensures that architectural and module boundary rules are not violated (dependencies flow inward, modules communicate only via inbound ports).

## 🚀 Getting Started

The project includes a `Makefile` to simplify local development and deployment.
Make sure that `./mvnw spotless:apply` is executed before compiling since there is a code formatting check.

### Prerequisites

* Docker & Docker Compose
* Java 25+ (if running locally)
* Make

### Run via Docker (Recommended)

To spin up the entire environment (the Spring Boot application, PostgreSQL, Kafka, and Kafka UI) inside isolated containers:

```bash
make docker
```

*This command will build the application image and use Docker Compose to start all required services.*

### Run Locally (Dev Mode)

To run the Spring Boot application using your local Java environment while spinning up infrastructure via Docker Compose:

```bash
make local
```

*This starts PostgreSQL and Kafka locally, then boots the Spring application with the `local` profile.*

## 📖 API Documentation & Endpoints

**Swagger UI / OpenAPI Spec:**
Available at `http://localhost:8080/swagger-ui.html` and `http://localhost:8080/api-docs` when the application is running.

**Core Endpoints:**

| Method | Endpoint | Description | Required Header |
| --- | --- | --- | --- |
| POST | `/api/users/register` | Register a new user | |
| POST | `/api/users/login` | Login and get JWT | |
| POST | `/api/users/refresh` | Refresh JWT | `Authorization` |
| GET  | `/api/users/profile` | Get user profile | `Authorization` |
| POST | `/api/accounts` | Create a new bank account | `Authorization` |
| POST | `/api/accounts/deposit` | Deposit money | `Authorization` |
| POST | `/api/accounts/withdraw` | Withdraw money | `Authorization` |
| POST | `/api/transfers` | Transfer between accounts | `Authorization` |
| GET  | `/api/transactions/{acc}` | Get transaction ledger | `Authorization` |

*(All endpoints require standard JSON payloads matching their respective commands).*

## Presentation

You can find the presentation [here](https://docs.google.com/presentation/d/1iG0XoW5Oz2vF8JDNDIScYzqoGzPl8HDYGn5Lu3MOCxk/edit?usp=sharing).