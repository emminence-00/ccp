# Secure Financial Services Management System
**CS-Java Complex Computing Problem (Assignment No. 3)**

## 1. Project Introduction
This project is a Java-based Secure Financial Services Management System. It simulates a full-stack banking environment, including user authentication, account management, transaction processing, and security layers. The system is designed using Object-Oriented Programming (OOP) principles and follows a multi-layered architecture for security and maintainability.

## 2. Core Features
- **Identity Management**: Secure registration and login with a 5-attempt lockout policy.
- **Account Services**: Support for Savings, Current, and Fixed Deposit accounts with automated interest calculation.
- **Transaction Engine**: Atomic processing of deposits, withdrawals, transfers, and bill payments with daily limit checks.
- **Loan Management**: Automated credit scoring, EMI calculation, and repayment scheduling.
- **Security & Audit**: RSA-2048 encryption for sensitive data, SHA-256 password hashing, and PII data masking.
- **Reporting**: Generation of account statements and an Admin dashboard for system oversight.

## 3. Technical Implementation
- **Language**: Java (JDK 17)
- **Data Structures**: `HashMap` for fast user/account retrieval and `ArrayList` for transaction history.
- **Precision Math**: `BigDecimal` is used for all financial calculations to ensure decimal accuracy.
- **Exception Handling**: A robust custom exception hierarchy (`InsufficientFundsException`, `LimitExceededException`, etc.) is used to manage business rule violations.

## 4. Project Structure
The source code is organized into logical packages (folders):
- `Main.java`: The main console controller and user interface.
- `models/`: Data entities representing users, accounts, and loans.
- `services/`: Business logic for authentication, transactions, and reporting.
- `utils/`: Security and validation utilities.
- `exceptions/`: Custom exception classes for error handling.

## 5. Instructions for Setup
1. Open the project in any Java IDE (IntelliJ, Eclipse, etc.) or a terminal.
2. Compile all source files:
   ```bash
   javac -d out src/*.java src/**/*.java
   ```
3. Run the main application:
   ```bash
   java -cp out Main
   ```
4. Run the automated verification test script:
   ```bash
   java -cp out -ea Verification
   ```
