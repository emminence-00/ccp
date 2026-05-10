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

    public Loan applyForLoan(User user, BigDecimal amount, int tenureMonths) {
        // Simple credit scoring logic
        double creditScore = calculateCreditScore(user);
        double annualRate = 15.0 - (creditScore / 100); // Higher score, lower rate

        Loan loan = new Loan(user, amount, annualRate, tenureMonths);
        if (creditScore > 500) {
            loan.setStatus(LoanStatus.APPROVED);
            loan.generateRepaymentSchedule();
        } else {
            loan.setStatus(LoanStatus.PENDING);
        }
        
        loans.add(loan);
        System.out.println("Loan application for " + user.getEmail() + " | Status: " + loan.getStatus());
        return loan;
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
