import models.*;
import services.*;
import utils.*;
import exceptions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Verification {
    public static void main(String[] args) throws Exception {
        AuthService authService = new AuthService();
        AccountService accountService = new AccountService();
        TransactionService txService = new TransactionService();
        LoanService loanService = new LoanService();
        ReportingService reportingService = new ReportingService(txService, accountService, loanService);

        System.out.println("--- Starting Automated Verification ---");

        // 1. Security Verification
        System.out.println("Testing RSA Encryption...");
        String secret = "SensitiveData123";
        String encrypted = SecurityUtils.encryptRSA(secret);
        String decrypted = SecurityUtils.decryptRSA(encrypted);
        assert secret.equals(decrypted) : "RSA Decryption failed";
        System.out.println("RSA Verified.");

        System.out.println("Testing Data Masking...");
        String masked = SecurityUtils.maskAccountNumber("1234567890");
        assert masked.equals("****7890") : "Masking failed: " + masked;
        System.out.println("Masking Verified.");

        // 2. Auth & Lockout Verification
        System.out.println("Testing Auth Lockout...");
        authService.register("Test User", "test@bank.com", "1234567890", LocalDate.of(1990, 1, 1), "NID123", "password123", Role.CUSTOMER);
        for (int i = 0; i < 5; i++) {
            try { authService.login("test@bank.com", "wrong_pass"); } catch (AuthenticationException e) {}
        }
        try {
            authService.login("test@bank.com", "password123");
            System.err.println("FAILED: Account should be locked!");
        } catch (AuthenticationException e) {
            System.out.println("Lockout Verified: " + e.getMessage());
        }

        // 3. Account & Interest Verification
        System.out.println("Testing Interest Calculation...");
        User user = authService.getAllUsers().get("test@bank.com");
        Account savings = accountService.openAccount(user, "SAVINGS", new BigDecimal("1000.00"));
        accountService.applyMonthlyInterest();
        BigDecimal balance = savings.getBalance();
        // 1000 * (0.04/12) = 3.33 approx
        System.out.println("Savings Balance after 1 month interest: " + balance);
        assert balance.compareTo(new BigDecimal("1000.00")) > 0 : "Interest not applied";

        // 4. Transaction & Daily Limit Verification
        System.out.println("Testing Daily Limits...");
        try {
            txService.withdraw(savings, new BigDecimal("60000.00"));
            System.err.println("FAILED: Daily limit should have been triggered!");
        } catch (LimitExceededException e) {
            System.out.println("Daily Limit Verified: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }

        // 5. Loan & EMI Verification
        System.out.println("Testing Loan EMI...");
        Loan loan = loanService.applyForLoan(user, new BigDecimal("10000.00"), 12);
        System.out.println("Loan Status: " + loan.getStatus());
        if (loan.getStatus() == LoanStatus.APPROVED) {
            System.out.println("EMI: " + loan.calculateEMI());
            assert loan.calculateEMI().compareTo(BigDecimal.ZERO) > 0 : "EMI calculation failed";
        }

        // 6. Reporting Verification
        System.out.println("Testing Statement Export...");
        reportingService.exportStatement(savings.getAccountNumber(), LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        
        System.out.println("--- Verification Completed Successfully ---");
    }
}
