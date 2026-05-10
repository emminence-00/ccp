package models;
import exceptions.*;


import java.math.BigDecimal;

public class CurrentAccount extends Account {
    public CurrentAccount(String accountNumber, User owner, BigDecimal initialBalance) {
        super(accountNumber, owner, initialBalance);
    }

    @Override
    public void calculateMonthlyInterest() {
        // Current accounts typically don't earn interest
    }
}
