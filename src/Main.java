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
        System.out.println("1. Open Account\n2. Deposit\n3. Withdraw\n4. Transfer\n5. Mini Statement\n6. Export Full Statement\n7. Apply for Loan\n8. View My Accounts\n9. Logout");
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
                case 8: handleViewAccounts(); break;
                case 9: 
                    authService.logout(); 
                    saveData();
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void showAdminMenu() {
        System.out.println("1. Admin Dashboard\n2. Apply Monthly Interest\n3. Check Overdue Loans\n4. Search Transactions\n5. Manage User Roles\n6. Logout");
        int choice = getIntInput();
        switch (choice) {
            case 1: reportingService.printAdminDashboard(); break;
            case 2: accountService.applyMonthlyInterest(); break;
            case 3: loanService.checkOverdueLoans(); break;
            case 4: handleSearchTransactions(); break;
            case 5: handleManageUserRoles(); break;
            case 6: 
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
        System.out.print("Loan Amount: ");
        BigDecimal amt = new BigDecimal(scanner.next());
        System.out.print("Tenure (months): ");
        int months = getIntInput();
        Loan loan = loanService.applyForLoan(authService.getCurrentUser(), amt, months);
        System.out.println("Loan " + loan.getLoanId() + " Status: " + loan.getStatus());
        if (loan.getStatus() == LoanStatus.APPROVED) {
            System.out.println("Monthly EMI: " + loan.calculateEMI());
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
