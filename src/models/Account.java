package models;
import exceptions.*;


import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class Account implements java.io.Serializable {
    private String accountNumber;
    private BigDecimal balance;
    private AccountStatus status;
    private User owner;

    public Account(String accountNumber, User owner, BigDecimal initialBalance) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = initialBalance.setScale(2, RoundingMode.HALF_UP);
        this.status = AccountStatus.ACTIVE;
    }

    public String getAccountNumber() { return accountNumber; }
    public BigDecimal getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public User getOwner() { return owner; }

    public void setStatus(AccountStatus status) { this.status = status; }

    public void deposit(BigDecimal amount) {
        // Increase balance if amount is positive
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            balance = balance.add(amount).setScale(2, RoundingMode.HALF_UP);
        }
    }

    public void withdraw(BigDecimal amount) throws InsufficientFundsException {
        // First check if user has enough money
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Error: Not enough money in account! Current balance is: " + balance);
        }
        
        // Subtract from balance
        balance = balance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
    }

    public abstract void calculateMonthlyInterest();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                ", status=" + status +
                ", owner=" + owner.getFullName() +
                '}';
    }
}
