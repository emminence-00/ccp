# Technical Documentation: Secure Financial Services Management System

## 1. Executive Summary
The **Secure Financial Services Management System** is a robust, console-based Java application designed to simulate modern banking operations. It emphasizes security, data integrity, and object-oriented design. The system supports complex financial workflows including user authentication, multi-tier account management, atomic transactions, and automated loan processing.

---

## 2. System Architecture
The application follows a **Modular Layered Architecture**, separating concerns into distinct packages for maintainability and security.

### 2.1 Package Structure
- **`models/`**: Domain entities representing data structures (User, Account, Loan, etc.).
- **`services/`**: Core business logic and rules engine.
- **`utils/`**: Shared utilities, specifically for Security and Data Masking.
- **`exceptions/`**: Custom Exception hierarchy for business rule validation.
- **`Main.java`**: Orchestration layer and User Interface (CLI).

---

## 3. Core Functional Modules

### 3.1 Identity & Access Management (IAM)
- **Authentication**: SHA-256 Hashing for password security.
- **Authorization**: Role-Based Access Control (RBAC) with three tiers: `CUSTOMER`, `TELLER`, and `ADMIN`.
- **Security Policy**: Automatic account lockout after 5 consecutive failed login attempts.

### 3.2 Account & Transaction Management
- **Account Types**: Savings (4% interest), Current (Standard), and Fixed Deposit (7% interest).
- **Transaction Engine**: Implements atomic operations for Deposits, Withdrawals, and Transfers.
- **Business Rules**: Daily withdrawal limits and strict balance validation using `BigDecimal`.

### 3.3 Credit & Loan Services
- **Loan Processing**: Includes an automated credit scoring simulator based on user account history.
- **Financial Calculation**: Precise EMI (Equated Monthly Installment) calculation using standard financial formulas.

---

## 4. Design & Implementation Details

### 4.1 Object-Oriented Principles
- **Encapsulation**: All data fields in models are private with controlled access through getters/setters.
- **Inheritance**: The `Account` abstract class serves as a blueprint for specific account types (`SavingsAccount`, etc.).
- **Polymorphism**: Used in the Transaction and Reporting services to handle different account types uniformly.

### 4.2 Security Layer
- **RSA-2048 Encryption**: Used for protecting PII (Personally Identifiable Information) during the registration and authentication phase.
- **Data Masking**: Sensitive identifiers like account numbers are masked (`****1234`) in public logs and statements.

### 4.3 Persistence Layer
- **Object Serialization**: The system uses Java's `Serializable` interface to save the entire bank state into a binary file (`bank_data.dat`), ensuring data persists across application restarts.

---

## 5. User Manual

### 5.1 Compilation
Run the following command from the project root:
```bash
javac -d out src/*.java src/**/*.java
```

### 5.2 Execution
Start the application:
```bash
java -cp out Main
```

### 5.3 Administrative Features
Login as an `ADMIN` to access:
- **Bank Dashboard**: Total deposits, active loans, and system health.
- **Interest Processing**: One-click monthly interest application for all savings accounts.
- **Audit Logs**: Search and review all system transactions.

---

## 6. Future Enhancements
- **Graphical User Interface (GUI)**: Transitioning from CLI to JavaFX or Swing.
- **Database Integration**: Moving from File Storage to a relational database like MySQL or PostgreSQL.
- **Networking**: Implementing a Client-Server model to allow multiple users to connect simultaneously.
