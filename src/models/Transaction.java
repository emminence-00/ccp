package models;
import exceptions.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

public class Transaction implements java.io.Serializable {
    private String transactionId;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private String description;

    public Transaction(TransactionType type, BigDecimal amount, String sourceAccountNumber, String destinationAccountNumber, String description) {
        // Generate a simple 8-character ID manually
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        this.transactionId = sb.toString();
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.sourceAccountNumber = sourceAccountNumber;
        this.destinationAccountNumber = destinationAccountNumber;
        this.description = description;
    }

    public String getTransactionId() { return transactionId; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getSourceAccountNumber() { return sourceAccountNumber; }
    public String getDestinationAccountNumber() { return destinationAccountNumber; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s | Source: %s | Dest: %s | ID: %s",
                timestamp, type, amount, sourceAccountNumber, destinationAccountNumber, transactionId);
    }
}
