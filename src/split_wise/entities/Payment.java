package split_wise.entities;

import java.time.Instant;

public class Payment implements Transaction {
    private final String id;
    private final User payer;
    private final User payee;
    private final double amount;
    private final Instant timestamp;

    public Payment(String id, User payer, User payee, double amount) {
        this.id = id;
        this.payer = payer;
        this.payee = payee;
        this.amount = amount;
        this.timestamp = Instant.now();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    public User getPayer() {
        return payer;
    }

    public User getPayee() {
        return payee;
    }

    public double getAmount() {
        return amount;
    }
}
