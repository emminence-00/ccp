package services;
import models.*;
import models.*;
import exceptions.*;
import utils.*;


import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ReportingService {
    private TransactionService txService;
    private AccountService accountService;
    private LoanService loanService;

    public ReportingService(TransactionService txService, AccountService accountService, LoanService loanService) {
        this.txService = txService;
        this.accountService = accountService;
        this.loanService = loanService;
    }

    public void printMiniStatement(String accountNumber) {
        List<Transaction> txs = txService.getHistoryForAccount(accountNumber);
        // Manual sorting and limit for student-level code
        Collections.sort(txs, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                return t2.getTimestamp().compareTo(t1.getTimestamp());
            }
        });
        
        for (int i = 0; i < Math.min(5, txs.size()); i++) {
            System.out.println(txs.get(i));
        }
    }

    public void exportStatement(String accountNumber, LocalDateTime start, LocalDateTime end) {
        List<Transaction> history = txService.getHistoryForAccount(accountNumber);
        List<Transaction> txs = new ArrayList<>();
        for (Transaction tx : history) {
            if (!tx.getTimestamp().isBefore(start) && !tx.getTimestamp().isAfter(end)) {
                txs.add(tx);
            }
        }
        
        Collections.sort(txs, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                return t1.getTimestamp().compareTo(t2.getTimestamp());
            }
        });

        String filename = "Statement_" + accountNumber + ".txt";
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("========================================\n");
            writer.write("        SECURE BANKING STATEMENT        \n");
            writer.write("========================================\n");
            writer.write("Account: " + SecurityUtils.maskAccountNumber(accountNumber) + "\n");
            writer.write("Period: " + start + " to " + end + "\n");
            writer.write("----------------------------------------\n");
            for (Transaction tx : txs) {
                writer.write(tx.toString() + "\n");
            }
            writer.write("========================================\n");
            System.out.println("Statement exported to " + filename);
        } catch (IOException e) {
            System.err.println("Failed to export statement: " + e.getMessage());
        }
    }

    public void printAdminDashboard() {
        Map<String, Account> accounts = accountService.getAllAccounts();
        List<Loan> loans = loanService.getAllLoans();
        List<Transaction> txs = txService.getAllTransactions();

        BigDecimal totalDeposits = BigDecimal.ZERO;
        for (Account acc : accounts.values()) {
            totalDeposits = totalDeposits.add(acc.getBalance());
        }

        BigDecimal totalLoans = BigDecimal.ZERO;
        for (Loan loan : loans) {
            if (loan.getStatus() != LoanStatus.CLOSED) {
                totalLoans = totalLoans.add(loan.getPrincipal());
            }
        }

        int flaggedCount = 0;
        BigDecimal flagLimit = new BigDecimal("100000");
        for (Transaction tx : txs) {
            if (tx.getAmount().compareTo(flagLimit) > 0) {
                flaggedCount++;
            }
        }
        System.out.println("========== ADMIN DASHBOARD ==========");
        System.out.println("Total Accounts: " + accounts.size());
        System.out.println("Total Deposits: " + totalDeposits);
        System.out.println("Outstanding Loans: " + totalLoans);
        System.out.println("Total Transactions: " + txs.size());
        System.out.println("Flagged Transactions: " + flaggedCount);
        System.out.println("=====================================");
    }
}
