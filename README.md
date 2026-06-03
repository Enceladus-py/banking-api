# Hexagonal Banking API

A robust, scalable banking REST API built with Spring Boot, demonstrating strict **Domain-Driven Design (DDD)** and **Hexagonal Architecture (Ports and Adapters)**.

This project goes beyond standard MVC to enforce strict business rules, isolated domain logic, and high-performance database interactions.

## 🏗 Architecture Overview

This application strictly adheres to Hexagonal Architecture, ensuring the core business logic is completely decoupled from frameworks, databases, and UI delivery mechanisms. The codebase is divided into three primary layers:

### 1. Domain Layer (domain)

The heart of the application. It contains pure Java models (Account, User, TransactionRecord).

* **Zero Dependencies:** No Spring annotations, no JPA, no web concepts.
* **Business Invariants:** Entities strictly protect their own state (e.g., Account.withdraw() throws an IllegalStateException if funds are insufficient).

### 2. Application Layer (application)

The orchestrator. It defines the "Ports" (interfaces) that allow the outside world to interact with the Domain.

* **Inbound Ports (Use Cases):** Interfaces defining what the system can do (e.g., DepositMoneyUseCase).
* **Outbound Ports (SPIs):** Interfaces defining what the system needs from the outside world (e.g., AccountRepository).

### 3. Infrastructure Layer (infrastructure/adapter)

The outer shell. It adapts external technologies to the core application's ports.

* **Inbound Adapters (Web):** REST Controllers (AccountController, TransferController) that translate HTTP requests and headers into Use Case Commands.
* **Outbound Adapters (Persistence):** Spring Data JPA repositories and entity mappers. Translates pure Domain objects into database rows and vice versa.

## ✨ Key Design Decisions

* **Strict Security Contract:** Authentication and authorization are enforced via an X-User-Id header. The core domain verifies account ownership before executing any mutations, preventing unauthorized access.
* **Fail-Fast Validation:** The API does not silently trim or fix malformed client data. Bad data is rejected at the boundary, enforcing strict API contracts.
* **JPA Optimization (Avoid SELECT before INSERT):** JPA entities implement Persistable. The Outbound Port is explicitly split into insert() and update(), allowing the adapter to control the isNew flag. This eliminates Hibernate's default behavior of executing an unnecessary SELECT query before inserting a new record.

## 🚀 Getting Started

The project includes a Makefile to simplify local development and deployment.
Make sure that `mvn spotless:apply` is executed before compiling since there is a check.

### Prerequisites

* Docker & Docker Compose
* Java 25+ (if running locally)
* Make

### Run via Docker (Recommended)

To spin up the entire environment (the Spring Boot application and the PostgreSQL database) inside isolated containers:

```bash
make docker
```

*This command will build the application image and use Docker Compose to start all required services.*

### Run Locally (Dev Mode)

To run the Spring Boot application using your local Java environment (useful for debugging in your IDE):

```bash
make local
```

*Note: Ensure your local database is running and configured in application.yml before using this command.*

## 📖 API Endpoints

| Method | Endpoint | Description | Required Header |
| --- | --- | --- | --- |
| POST | /api/accounts | Create a new bank account | X-User-Id |
| POST | /api/accounts/deposit | Deposit money | X-User-Id |
| POST | /api/accounts/withdraw | Withdraw money | X-User-Id |
| POST | /api/transfers | Transfer between accounts | X-User-Id |
| GET | /api/transactions/{acc} | Get transaction ledger | X-User-Id |

*(All endpoints require standard JSON payloads matching their respective commands).*