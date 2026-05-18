import models.*;
import services.*;
import utils.*;
import exceptions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static AuthService authService = new AuthService();
    private static AccountService accountService = new AccountService();
    private static TransactionService transactionService = new TransactionService();
    private static LoanService loanService = new LoanService();
    private static ReportingService reportingService = new ReportingService(transactionService, accountService, loanService);

    public static void main(String[] args) {
        loanService.setServices(accountService, transactionService);
        loadData();
        System.out.println("Welcome to the Secure Financial Services Management System");
        
        while (true) {
            if (authService.getCurrentUser() == null) {
                showGuestMenu();
            } else {
                showUserMenu();
            }
        }
    }

    private static void showGuestMenu() {
        System.out.println("\n1. Login\n2. Register\n3. Exit");
        int choice = getIntInput();
        switch (choice) {
            case 1: handleLogin(); break;
            case 2: handleRegistration(); break;
            case 3: 
                saveData();
                System.exit(0);
        }
    }

    private static void handleLogin() {
        System.out.print("Email: ");
        String email = scanner.next();
        System.out.print("Password: ");
        String password = scanner.next();
        try {
            authService.login(email, password);
        } catch (AuthenticationException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void handleRegistration() {
        System.out.print("Full Name: ");
        scanner.nextLine(); // consume
        String name = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.next();
        System.out.print("Phone: ");
        String phone = scanner.next();
        System.out.print("National ID: ");
        String nid = scanner.next();
        System.out.print("DOB (YYYY-MM-DD): ");
        LocalDate dob = LocalDate.parse(scanner.next());
        System.out.print("Password: ");
        String pw = scanner.next();

        try {
            authService.register(name, email, phone, dob, nid, pw, Role.CUSTOMER);
            System.out.println("Registration successful.");
        } catch (AuthenticationException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void showUserMenu() {
        User user = authService.getCurrentUser();
        System.out.println("\n--- Welcome, " + user.getFullName() + " [" + user.getRole() + "] ---");
        
        if (user.getRole() == Role.ADMIN) {
            showAdminMenu();
        } else {
            showCustomerMenu();
        }
    }

    private static void showCustomerMenu() {
        System.out.println("1. Open Account\n2. Deposit\n3. Withdraw\n4. Transfer\n5. Mini Statement\n6. Export Full Statement\n7. Apply for Loan\n8. View My Loans\n9. Pay Loan EMI\n10. View My Accounts\n11. Logout");
        int choice = getIntInput();
        try {
            switch (choice) {
                case 1: handleOpenAccount(); break;
                case 2: handleDeposit(); break;
                case 3: handleWithdraw(); break;
                case 4: handleTransfer(); break;
                case 5: handleMiniStatement(); break;
                case 6: handleExportStatement(); break;
                case 7: handleLoanApplication(); break;
                case 8: handleViewLoans(); break;
                case 9: handlePayEMI(); break;
                case 10: handleViewAccounts(); break;
                case 11: 
                    authService.logout(); 
                    saveData();
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void showAdminMenu() {
        System.out.println("1. Admin Dashboard\n2. Apply Monthly Interest\n3. Check Overdue Loans\n4. Search Transactions\n5. Manage User Roles\n6. Approve/Reject Pending Loans\n7. Logout");
        int choice = getIntInput();
        switch (choice) {
            case 1: reportingService.printAdminDashboard(); break;
            case 2: accountService.applyMonthlyInterest(); break;
            case 3: loanService.checkOverdueLoans(); break;
            case 4: handleSearchTransactions(); break;
            case 5: handleManageUserRoles(); break;
            case 6: handleApprovePendingLoans(); break;
            case 7: 
                authService.logout(); 
                saveData();
                break;
        }
    }

    private static void handleOpenAccount() {
        System.out.println("Type (SAVINGS, CURRENT, FIXED): ");
        String type = scanner.next();
        System.out.print("Initial Deposit: ");
        BigDecimal deposit = new BigDecimal(scanner.next());
        Account acc = accountService.openAccount(authService.getCurrentUser(), type, deposit);
        System.out.println("Account created: " + acc.getAccountNumber());
    }

    private static void handleDeposit() {
        Account acc = selectAccount();
        if (acc != null) {
            System.out.print("Amount: ");
            BigDecimal amt = new BigDecimal(scanner.next());
            transactionService.deposit(acc, amt);
        }
    }

    private static void handleWithdraw() {
        try {
            Account acc = selectAccount();
            if (acc != null) {
                System.out.print("Enter amount to withdraw: ");
                BigDecimal amt = new BigDecimal(scanner.next());
                
                // This calls the service which throws exceptions
                transactionService.withdraw(acc, amt);
            }
        } catch (LimitExceededException e) {
            System.err.println("Limit Error: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.err.println("Funds Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Something went wrong: " + e.getMessage());
        }
    }

    private static void handleTransfer() {
        try {
            Account from = selectAccount();
            if (from != null) {
                System.out.print("Enter Destination Account Number: ");
                String toNum = scanner.next();
                Account to = accountService.getAccount(toNum);
                
                if (to != null) {
                    System.out.print("Enter amount to transfer: ");
                    BigDecimal amt = new BigDecimal(scanner.next());
                    
                    // Logic: withdraw from one, deposit to other
                    transactionService.transfer(from, to, amt);
                } else {
                    System.out.println("Error: The destination account number does not exist!");
                }
            }
        } catch (LimitExceededException e) {
            System.err.println("Transfer failed: Limit reached. " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.err.println("Transfer failed: Not enough balance. " + e.getMessage());
        } catch (Exception e) {
            System.err.println("System Error during transfer: " + e.getMessage());
        }
    }

    private static void handleMiniStatement() {
        Account acc = selectAccount();
        if (acc != null) reportingService.printMiniStatement(acc.getAccountNumber());
    }

    private static void handleExportStatement() {
        Account acc = selectAccount();
        if (acc != null) {
            reportingService.exportStatement(acc.getAccountNumber(), LocalDateTime.now().minusMonths(1), LocalDateTime.now());
        }
    }

    private static void handleLoanApplication() {
        // First check if user has accounts
        List<Account> myAccs = accountService.getAccountsForUser(authService.getCurrentUser());
        if (myAccs.isEmpty()) {
            System.out.println("Error: You must open an account first to receive loan funds!");
            return;
        }

        System.out.println("Select Account to receive Loan Disbursement:");
        for (int i = 0; i < myAccs.size(); i++) {
            System.out.println((i + 1) + ". " + myAccs.get(i).getAccountNumber() + " (" + myAccs.get(i).getBalance() + ")");
        }
        int idx = getIntInput() - 1;
        if (idx < 0 || idx >= myAccs.size()) {
            System.out.println("Invalid account selection.");
            return;
        }
        Account selectedAcc = myAccs.get(idx);

        System.out.print("Loan Amount: ");
        BigDecimal amt = new BigDecimal(scanner.next());
        System.out.print("Tenure (months): ");
        int months = getIntInput();
        
        Loan loan = loanService.applyForLoan(authService.getCurrentUser(), amt, months, selectedAcc.getAccountNumber());
        System.out.println("Loan " + loan.getLoanId() + " Status: " + loan.getStatus());
        if (loan.getStatus() == LoanStatus.ACTIVE) {
            System.out.println("Monthly EMI: " + loan.calculateEMI());
            System.out.println("Funds of " + amt + " successfully deposited to account " + selectedAcc.getAccountNumber());
        } else if (loan.getStatus() == LoanStatus.PENDING) {
            System.out.println("Loan is pending manual review due to credit check.");
        }
        saveData();
    }

    private static void handleViewLoans() {
        List<Loan> myLoans = loanService.getLoansForUser(authService.getCurrentUser());
        if (myLoans.isEmpty()) {
            System.out.println("You have no loans.");
            return;
        }
        System.out.println("--- Your Loans ---");
        for (Loan l : myLoans) {
            System.out.println(String.format("Loan ID: %s | Principal: %s | Status: %s | Rate: %.2f%% | Tenure: %d months",
                    l.getLoanId(), l.getPrincipal(), l.getStatus(), l.getAnnualRate(), l.getTenureMonths()));
            if (!l.getRepaymentSchedule().isEmpty()) {
                System.out.println("  Repayment Schedule:");
                for (int i = 0; i < l.getRepaymentSchedule().size(); i++) {
                    Loan.Repayment r = l.getRepaymentSchedule().get(i);
                    System.out.println(String.format("    EMI %d: Due: %s | Amount: %s | Paid: %b",
                            (i + 1), r.getDueDate(), r.getAmount(), r.isPaid()));
                }
            }
        }
    }

    private static void handlePayEMI() {
        List<Loan> myLoans = loanService.getLoansForUser(authService.getCurrentUser());
        List<Loan> activeLoans = new ArrayList<>();
        for (Loan l : myLoans) {
            if (l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.DEFAULTED) {
                activeLoans.add(l);
            }
        }
        if (activeLoans.isEmpty()) {
            System.out.println("You have no active loans to pay.");
            return;
        }

        System.out.println("Select Loan to pay EMI:");
        for (int i = 0; i < activeLoans.size(); i++) {
            Loan l = activeLoans.get(i);
            // Find next unpaid repayment
            Loan.Repayment nextRep = null;
            for (Loan.Repayment r : l.getRepaymentSchedule()) {
                if (!r.isPaid()) {
                    nextRep = r;
                    break;
                }
            }
            if (nextRep != null) {
                System.out.println(String.format("%d. Loan %s | EMI Amount: %s (Due: %s)",
                        (i + 1), l.getLoanId(), nextRep.getAmount(), nextRep.getDueDate()));
            } else {
                System.out.println(String.format("%d. Loan %s | Fully Paid", (i + 1), l.getLoanId()));
            }
        }

        int loanIdx = getIntInput() - 1;
        if (loanIdx < 0 || loanIdx >= activeLoans.size()) {
            System.out.println("Invalid loan selection.");
            return;
        }
        Loan selectedLoan = activeLoans.get(loanIdx);

        // Find next unpaid repayment
        Loan.Repayment repaymentToPay = null;
        for (Loan.Repayment r : selectedLoan.getRepaymentSchedule()) {
            if (!r.isPaid()) {
                repaymentToPay = r;
                break;
            }
        }
        if (repaymentToPay == null) {
            System.out.println("This loan is already fully repaid!");
            return;
        }

        // Select account to pay from
        List<Account> myAccs = accountService.getAccountsForUser(authService.getCurrentUser());
        if (myAccs.isEmpty()) {
            System.out.println("Error: You have no active accounts to pay from!");
            return;
        }
        System.out.println("Select Account to pay EMI from:");
        for (int i = 0; i < myAccs.size(); i++) {
            System.out.println((i + 1) + ". " + myAccs.get(i).getAccountNumber() + " (" + myAccs.get(i).getBalance() + ")");
        }
        int accIdx = getIntInput() - 1;
        if (accIdx < 0 || accIdx >= myAccs.size()) {
            System.out.println("Invalid account selection.");
            return;
        }
        Account selectedAcc = myAccs.get(accIdx);

        try {
            // Withdraw EMI amount
            transactionService.withdraw(selectedAcc, repaymentToPay.getAmount());
            repaymentToPay.setPaid(true);
            System.out.println(String.format("Successfully paid EMI of %s from account %s",
                    repaymentToPay.getAmount(), selectedAcc.getAccountNumber()));

            // Check if all paid
            boolean allPaid = true;
            for (Loan.Repayment r : selectedLoan.getRepaymentSchedule()) {
                if (!r.isPaid()) {
                    allPaid = false;
                    break;
                }
            }
            if (allPaid) {
                selectedLoan.setStatus(LoanStatus.CLOSED);
                System.out.println("Congratulations! Loan " + selectedLoan.getLoanId() + " is now fully REPAID.");
            }
            saveData();
        } catch (Exception e) {
            System.err.println("EMI Payment failed: " + e.getMessage());
        }
    }

    private static void handleApprovePendingLoans() {
        List<Loan> allLoans = loanService.getAllLoans();
        List<Loan> pendingLoans = new ArrayList<>();
        for (Loan l : allLoans) {
            if (l.getStatus() == LoanStatus.PENDING) {
                pendingLoans.add(l);
            }
        }
        if (pendingLoans.isEmpty()) {
            System.out.println("No pending loans at this time.");
            return;
        }

        System.out.println("--- Pending Loan Applications ---");
        for (int i = 0; i < pendingLoans.size(); i++) {
            Loan l = pendingLoans.get(i);
            System.out.println(String.format("%d. Loan ID: %s | Borrower: %s (%s) | Amount: %s | Tenure: %d months",
                    (i + 1), l.getLoanId(), l.getBorrower().getFullName(), l.getBorrower().getEmail(),
                    l.getPrincipal(), l.getTenureMonths()));
        }

        System.out.print("Select loan number to action: ");
        int idx = getIntInput() - 1;
        if (idx < 0 || idx >= pendingLoans.size()) {
            System.out.println("Invalid loan selection.");
            return;
        }
        Loan selectedLoan = pendingLoans.get(idx);

        System.out.println("1. Approve\n2. Reject\n3. Cancel Action");
        int decision = getIntInput();
        if (decision == 1) {
            // Find active accounts for the borrower
            List<Account> borrowerAccs = accountService.getAccountsForUser(selectedLoan.getBorrower());
            if (borrowerAccs.isEmpty()) {
                System.out.println("Error: Borrower does not have any active accounts to disburse funds to!");
                return;
            }
            System.out.println("Select Borrower Account for disbursement:");
            for (int i = 0; i < borrowerAccs.size(); i++) {
                System.out.println((i + 1) + ". " + borrowerAccs.get(i).getAccountNumber() + " (" + borrowerAccs.get(i).getBalance() + ")");
            }
            int accIdx = getIntInput() - 1;
            if (accIdx < 0 || accIdx >= borrowerAccs.size()) {
                System.out.println("Invalid account selection.");
                return;
            }
            Account selectedAcc = borrowerAccs.get(accIdx);
            loanService.approveLoan(selectedLoan, selectedAcc);
            saveData();
        } else if (decision == 2) {
            loanService.rejectLoan(selectedLoan);
            saveData();
        } else {
            System.out.println("Action cancelled.");
        }
    }

    private static void handleViewAccounts() {
        List<Account> myAccs = accountService.getAccountsForUser(authService.getCurrentUser());
        for (Account a : myAccs) {
            System.out.println(a);
        }
    }

    private static void handleSearchTransactions() {
        System.out.print("Search Query (ID or Description): ");
        String query = scanner.next();
        List<Transaction> res = transactionService.searchTransactions(query);
        res.forEach(System.out::println);
    }

    private static void handleManageUserRoles() {
        System.out.print("Enter User Email: ");
        String email = scanner.next();
        User targetUser = authService.getAllUsers().get(email);
        if (targetUser == null) {
            System.out.println("Error: User not found!");
            return;
        }
        System.out.println("Current role for " + targetUser.getFullName() + " is " + targetUser.getRole());
        System.out.println("Select New Role (1. CUSTOMER, 2. TELLER, 3. ADMIN): ");
        int r = getIntInput();
        if (r < 1 || r > 3) {
            System.out.println("Invalid role choice.");
            return;
        }
        Role newRole = (r == 3) ? Role.ADMIN : (r == 2) ? Role.TELLER : Role.CUSTOMER;
        targetUser.setRole(newRole);
        System.out.println("Successfully updated role to " + newRole + " for " + targetUser.getEmail());
        saveData();
    }

    private static Account selectAccount() {
        List<Account> myAccs = accountService.getAccountsForUser(authService.getCurrentUser());
        if (myAccs.isEmpty()) {
            System.out.println("No accounts found.");
            return null;
        }
        System.out.println("Select Account:");
        for (int i = 0; i < myAccs.size(); i++) {
            System.out.println((i + 1) + ". " + myAccs.get(i).getAccountNumber() + " (" + myAccs.get(i).getBalance() + ")");
        }
        int idx = getIntInput() - 1;
        return (idx >= 0 && idx < myAccs.size()) ? myAccs.get(idx) : null;
    }

    @SuppressWarnings("unchecked")
    private static void loadData() {
        Map<String, Object> data = StorageService.loadState();
        if (data != null) {
            authService.setUsers((Map<String, User>) data.get("users"));
            accountService.setAccounts((Map<String, Account>) data.get("accounts"));
            transactionService.setTransactions((List<Transaction>) data.get("transactions"));
            loanService.setLoans((List<Loan>) data.get("loans"));
            System.out.println("Previous session data loaded.");
        }
    }

    private static void saveData() {
        StorageService.saveState(
            authService.getAllUsers(),
            accountService.getAllAccounts(),
            transactionService.getAllTransactions(),
            loanService.getAllLoans()
        );
    }

    private static int getIntInput() {
        try {
            return scanner.nextInt();
        } catch (Exception e) {
            scanner.nextLine(); // clear
            return -1;
        }
    }
}
