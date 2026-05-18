package services;
import models.*;
import exceptions.*;
import utils.*;


import java.math.BigDecimal;
import java.util.*;

public class TransactionService {
    private List<Transaction> transactionHistory = new ArrayList<>();
    private Map<String, BigDecimal> dailySpent = new HashMap<>(); // email -> amount
    private static final BigDecimal DAILY_WITHDRAWAL_LIMIT = new BigDecimal("50000.00");

    public void deposit(Account account, BigDecimal amount) {
        account.deposit(amount);
        Transaction tx = new Transaction(TransactionType.DEPOSIT, amount, null, account.getAccountNumber(), "Cash Deposit");
        transactionHistory.add(tx);
        System.out.println("Deposit of " + amount + " successful.");
    }

    public void withdraw(Account account, BigDecimal amount) throws LimitExceededException, InsufficientFundsException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Error: Withdrawal amount must be positive!");
        }
        // First check daily limit
        checkDailyLimit(account.getOwner(), amount);
        
        // Then try to withdraw from account (this throws InsufficientFundsException)
        account.withdraw(amount);
        
        Transaction tx = new Transaction(TransactionType.WITHDRAWAL, amount, account.getAccountNumber(), null, "Cash Withdrawal");
        transactionHistory.add(tx);
        updateDailyLimit(account.getOwner(), amount);
        System.out.println("Withdrawal of " + amount + " successful.");
    }

    public void transfer(Account from, Account to, BigDecimal amount) throws LimitExceededException, InsufficientFundsException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Error: Transfer amount must be positive!");
        }
        // Check daily limit for sender
        checkDailyLimit(from.getOwner(), amount);
        
        // Withdraw from source
        from.withdraw(amount);
        
        // Deposit to destination
        to.deposit(amount);
        
        Transaction tx = new Transaction(TransactionType.TRANSFER, amount, from.getAccountNumber(), to.getAccountNumber(), "Fund Transfer");
        transactionHistory.add(tx);
        updateDailyLimit(from.getOwner(), amount);
        System.out.println("Transfer of " + amount + " successful.");
    }

    public void payBill(Account account, String biller, BigDecimal amount) throws LimitExceededException, InsufficientFundsException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Error: Bill payment amount must be positive!");
        }
        checkDailyLimit(account.getOwner(), amount);
        account.withdraw(amount);
        Transaction tx = new Transaction(TransactionType.BILL_PAYMENT, amount, account.getAccountNumber(), biller, "Bill Payment");
        transactionHistory.add(tx);
        updateDailyLimit(account.getOwner(), amount);
        System.out.println("Bill payment of " + amount + " to " + biller + " successful.");
    }

    private void checkDailyLimit(User user, BigDecimal amount) throws LimitExceededException {
        String key = user.getEmail() + "_" + java.time.LocalDate.now();
        BigDecimal spentToday = dailySpent.getOrDefault(key, BigDecimal.ZERO);
        if (spentToday.add(amount).compareTo(DAILY_WITHDRAWAL_LIMIT) > 0) {
            throw new LimitExceededException("Daily limit of " + DAILY_WITHDRAWAL_LIMIT + " exceeded.");
        }
    }

    private void updateDailyLimit(User user, BigDecimal amount) {
        String key = user.getEmail() + "_" + java.time.LocalDate.now();
        BigDecimal spentToday = dailySpent.getOrDefault(key, BigDecimal.ZERO);
        dailySpent.put(key, spentToday.add(amount));
    }

    public List<Transaction> getHistoryForAccount(String accountNumber) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction tx : transactionHistory) {
            if (accountNumber.equals(tx.getSourceAccountNumber()) || accountNumber.equals(tx.getDestinationAccountNumber())) {
                result.add(tx);
            }
        }
        return result;
    }

    public List<Transaction> searchTransactions(String query) {
        List<Transaction> result = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Transaction tx : transactionHistory) {
            if (tx.toString().toLowerCase().contains(lowerQuery) || tx.getTransactionId().equalsIgnoreCase(query)) {
                result.add(tx);
            }
        }
        return result;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactionHistory = transactions;
    }

    public List<Transaction> getAllTransactions() { return transactionHistory; }
}
