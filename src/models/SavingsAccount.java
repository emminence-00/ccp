package models;
import exceptions.*;


import java.math.BigDecimal;
import java.math.RoundingMode;

public class SavingsAccount extends Account {
    private static final BigDecimal ANNUAL_INTEREST_RATE = new BigDecimal("0.04"); // 4%

    public SavingsAccount(String accountNumber, User owner, BigDecimal initialBalance) {
        super(accountNumber, owner, initialBalance);
    }

    @Override
    public void calculateMonthlyInterest() {
        // Compound interest formula: A = P(1 + r/n)^(nt)
        // Monthly calculation: P * (r/12)
        BigDecimal monthlyRate = ANNUAL_INTEREST_RATE.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        BigDecimal interest = getBalance().multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
        deposit(interest);
    }
}
