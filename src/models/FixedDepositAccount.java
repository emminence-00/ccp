package models;
import exceptions.*;


import java.math.BigDecimal;
import java.math.RoundingMode;

public class FixedDepositAccount extends Account {
    private static final BigDecimal ANNUAL_INTEREST_RATE = new BigDecimal("0.07"); // 7%

    public FixedDepositAccount(String accountNumber, User owner, BigDecimal initialBalance) {
        super(accountNumber, owner, initialBalance);
    }

    @Override
    public void withdraw(BigDecimal amount) throws InsufficientFundsException {
        throw new InsufficientFundsException("Error: Cannot withdraw from a Fixed Deposit account before maturity!");
    }

    @Override
    public void calculateMonthlyInterest() {
        BigDecimal monthlyRate = ANNUAL_INTEREST_RATE.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
        BigDecimal interest = getBalance().multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
        deposit(interest);
    }
}
