package models;
import exceptions.*;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Loan implements java.io.Serializable {
    private String loanId;
    private User borrower;
    private BigDecimal principal;
    private double annualRate;
    private int tenureMonths;
    private LoanStatus status;
    private LocalDate startDate;
    private String destinationAccountNumber;
    private List<Repayment> repaymentSchedule;

    public Loan(User borrower, BigDecimal principal, double annualRate, int tenureMonths, String destinationAccountNumber) {
        // Generate a simple 8-character ID manually
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        this.loanId = sb.toString();
        this.borrower = borrower;
        this.principal = principal;
        this.annualRate = annualRate;
        this.tenureMonths = tenureMonths;
        this.destinationAccountNumber = destinationAccountNumber;
        this.status = LoanStatus.PENDING;
        this.repaymentSchedule = new ArrayList<>();
    }

    public BigDecimal calculateEMI() {
        double r = (annualRate / 100) / 12; // monthly rate
        double n = tenureMonths;
        double p = principal.doubleValue();

        // Formula: [P x R x (1+R)^N / ((1+R)^N – 1)]
        double emi = (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
        return new BigDecimal(emi).setScale(2, RoundingMode.HALF_UP);
    }

    public void generateRepaymentSchedule() {
        BigDecimal emi = calculateEMI();
        LocalDate currentDate = LocalDate.now();
        for (int i = 1; i <= tenureMonths; i++) {
            repaymentSchedule.add(new Repayment(currentDate.plusMonths(i), emi));
        }
    }

    public String getLoanId() { return loanId; }
    public User getBorrower() { return borrower; }
    public BigDecimal getPrincipal() { return principal; }
    public double getAnnualRate() { return annualRate; }
    public int getTenureMonths() { return tenureMonths; }
    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public String getDestinationAccountNumber() { return destinationAccountNumber; }
    public void setDestinationAccountNumber(String destinationAccountNumber) { this.destinationAccountNumber = destinationAccountNumber; }
    public List<Repayment> getRepaymentSchedule() { return repaymentSchedule; }

    public static class Repayment implements java.io.Serializable {
        private LocalDate dueDate;
        private BigDecimal amount;
        private boolean isPaid;

        public Repayment(LocalDate dueDate, BigDecimal amount) {
            this.dueDate = dueDate;
            this.amount = amount;
            this.isPaid = false;
        }

        public LocalDate getDueDate() { return dueDate; }
        public BigDecimal getAmount() { return amount; }
        public boolean isPaid() { return isPaid; }
        public void setPaid(boolean paid) { isPaid = paid; }
    }
}
