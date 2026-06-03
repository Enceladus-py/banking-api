# Data-Backed Agents Architecture

Based on the Hexagonal Banking API's strict domain models (`User`, `Account`, `TransactionRecord`) and its underlying database infrastructure, the following AI Agents can be introduced. These agents are "data-backed", meaning they directly interact with the application's persistent state and respect the domain's strict invariants.

## 1. Customer Support Agent
**Domain Context:** `User`, `Account`
**Data Access:** Read-only via `AccountQueryService` and `UserService` (ports: `UserRepository`, `AccountRepository`).
**Responsibilities:**
- Respond to customer inquiries like "What is my current balance?" and "Can you show my account details?"
- Enforce the same security contract (`X-User-Id` header) used in the web adapters to ensure the agent only queries data for the authenticated user.
- **Available Tools:**
  - `getUserProfile(userId)`
  - `getAccountDetails(userId, accountNumber)`

## 2. Transaction Auditor & Fraud Detection Agent
**Domain Context:** `TransactionRecord`, Outbox Events
**Data Access:** `TransactionQueryService`, `OutboxEventRepository`
**Responsibilities:**
- Monitor `TransactionRecord` logs for suspicious activity (e.g., rapid consecutive transfers or unusually high withdrawal volumes).
- Correlate `PENDING` and `FAILED` outbox events (`OutboxStatus`) to detect stuck transactions and alert engineers.
- Ensure the system's eventual consistency (Outbox Pattern) is completing successfully by auditing `ProcessedEventJpaEntity`.
- **Available Tools:**
  - `getRecentTransactions(accountNumber)`
  - `findFailedTransactions()`
  - `analyzeTransferPatterns()`

## 3. Financial Advisor Agent
**Domain Context:** `TransactionRecord`, `Account`
**Data Access:** Aggregated read access to historical transactions via `GetAccountTransactionsUseCase`.
**Responsibilities:**
- Provide personalized financial insights based on the user's ledger history.
- Summarize cash flow by analyzing `DEPOSIT`, `WITHDRAWAL`, and `TRANSFER` transaction types over time.
- **Available Tools:**
  - `calculateMonthlyCashFlow(accountNumber, month)`
  - `getLargestTransactions(accountNumber)`

## 4. Administrative Operations Agent
**Domain Context:** `User`, `Account`, System Health
**Data Access:** Write-access via Application Layer Use Cases (`RegisterUserUseCase`, `CreateAccountUseCase`).
**Responsibilities:**
- Automate onboarding workflows for new customers (triggering user registration and initial account creation).
- Requires elevated administrative privileges, bypassing standard `X-User-Id` ownership checks for creation tasks.
- **Available Tools:**
  - `registerUser(name, surname)`
  - `createAccount(ownerId)`

## Integration Guidelines
To maintain the Hexagonal Architecture:
1. **No direct DB queries:** Agents must act as new **Inbound Adapters**, invoking the existing Inbound Ports (Use Cases).
2. **Respect Domain Rules:** Agents cannot mutate `Account` balances directly. They must dispatch commands (e.g., `DepositCommand`, `WithdrawCommand`) to ensure domain invariants (like `InsufficientFundsException`) are triggered properly.
3. **Code Formatting:** Make sure that `mvn spotless:apply` is executed before compiling since there is a check.
