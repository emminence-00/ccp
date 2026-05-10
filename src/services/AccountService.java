package services;
import models.*;
import exceptions.*;
import utils.*;


import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class AccountService {
    private Map<String, Account> accounts = new HashMap<>();
    private Random random = new Random();

    public Account openAccount(User owner, String type, BigDecimal initialDeposit) {
        String accountNumber = generateUniqueAccountNumber();
        Account account;
        switch (type.toUpperCase()) {
            case "SAVINGS":
                account = new SavingsAccount(accountNumber, owner, initialDeposit);
                break;
            case "FIXED":
                account = new FixedDepositAccount(accountNumber, owner, initialDeposit);
                break;
            case "CURRENT":
            default:
                account = new CurrentAccount(accountNumber, owner, initialDeposit);
                break;
        }
        accounts.put(accountNumber, account);
        System.out.println("Account opened: " + accountNumber + " for " + owner.getEmail());
        return account;
    }

    private String generateUniqueAccountNumber() {
        String num = "";
        do {
            num = "";
            for (int i = 0; i < 10; i++) {
                num += random.nextInt(10);
            }
        } while (accounts.containsKey(num));
        return num;
    }

    public Account getAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public List<Account> getAccountsForUser(User user) {
        List<Account> userAccounts = new ArrayList<>();
        for (Account account : accounts.values()) {
            if (account.getOwner().getEmail().equals(user.getEmail())) {
                userAccounts.add(account);
            }
        }
        return userAccounts;
    }

    public void applyMonthlyInterest() {
        for (Account account : accounts.values()) {
            if (account.getStatus() == AccountStatus.ACTIVE) {
                account.calculateMonthlyInterest();
            }
        }
        System.out.println("Monthly interest applied to all active accounts.");
    }

    public void closeAccount(String accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account != null) {
            account.setStatus(AccountStatus.CLOSED);
            System.out.println("Account closed: " + accountNumber);
        }
    }

    public void setAccounts(Map<String, Account> accounts) {
        this.accounts = accounts;
    }

    public Map<String, Account> getAllAccounts() { return accounts; }
}
