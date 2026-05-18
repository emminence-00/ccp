package services;
import models.*;
import exceptions.*;
import utils.*;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class LoanService {
    private List<Loan> loans = new ArrayList<>();
    private AccountService accountService;
    private TransactionService transactionService;

    public void setServices(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    public Loan applyForLoan(User user, BigDecimal amount, int tenureMonths, String destinationAccountNumber) {
        // Simple credit scoring logic
        double creditScore = calculateCreditScore(user);
        double annualRate = 15.0 - (creditScore / 100); // Higher score, lower rate

        Loan loan = new Loan(user, amount, annualRate, tenureMonths, destinationAccountNumber);
        if (creditScore > 500) {
            loan.setStatus(LoanStatus.ACTIVE);
            loan.setStartDate(LocalDate.now());
            loan.generateRepaymentSchedule();
            
            // Disburse funds immediately!
            if (accountService != null && transactionService != null) {
                Account account = accountService.getAccount(destinationAccountNumber);
                if (account != null) {
                    transactionService.deposit(account, amount);
                    System.out.println("Disbursed loan amount " + amount + " to account " + destinationAccountNumber);
                }
            }
        } else {
            loan.setStatus(LoanStatus.PENDING);
        }
        
        loans.add(loan);
        System.out.println("Loan application for " + user.getEmail() + " | Status: " + loan.getStatus());
        return loan;
    }

    public void approveLoan(Loan loan, Account account) {
        if (loan.getStatus() == LoanStatus.PENDING) {
            loan.setStatus(LoanStatus.ACTIVE);
            loan.setStartDate(LocalDate.now());
            loan.generateRepaymentSchedule();
            loan.setDestinationAccountNumber(account.getAccountNumber());
            if (transactionService != null) {
                transactionService.deposit(account, loan.getPrincipal());
            } else {
                account.deposit(loan.getPrincipal());
            }
            System.out.println("Loan " + loan.getLoanId() + " successfully approved and disbursed!");
        }
    }

    public void rejectLoan(Loan loan) {
        if (loan.getStatus() == LoanStatus.PENDING) {
            loan.setStatus(LoanStatus.CLOSED);
            System.out.println("Loan " + loan.getLoanId() + " has been rejected.");
        }
    }

    private double calculateCreditScore(User user) {
        // Simulation: Random score between 300 and 850
        return 300 + (Math.random() * 550);
    }

    public List<Loan> getLoansForUser(User user) {
        List<Loan> userLoans = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.getBorrower().getEmail().equals(user.getEmail())) {
                userLoans.add(loan);
            }
        }
        return userLoans;
    }

    public void checkOverdueLoans() {
        LocalDate today = LocalDate.now();
        for (Loan loan : loans) {
            if (loan.getStatus() == LoanStatus.ACTIVE) {
                for (Loan.Repayment r : loan.getRepaymentSchedule()) {
                    if (!r.isPaid() && r.getDueDate().isBefore(today)) {
                        loan.setStatus(LoanStatus.DEFAULTED);
                        System.out.println("Loan " + loan.getLoanId() + " marked as DEFAULTED.");
                        break;
                    }
                }
            }
        }
    }

    public void setLoans(List<Loan> loans) {
        this.loans = loans;
    }

    public List<Loan> getAllLoans() { return loans; }
}
