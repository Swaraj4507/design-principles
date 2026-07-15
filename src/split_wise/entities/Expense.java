package split_wise.entities;

import split_wise.strategy.SplitType;

import java.time.Instant;
import java.util.List;

public class Expense implements Transaction {
    private final String id;
    private final String description;
    private final double amount;
    private final User paidBy;
    private final SplitType splitType;
    private final List<Split> splits;
    private final Instant timestamp;

    public Expense(String id, String description, double amount, User paidBy, SplitType splitType, List<Split> splits) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy;
        this.splitType = splitType;
        this.splits = splits;
        this.timestamp = Instant.now();
    }

    public SplitType getSplitType() {
        return splitType;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public User getPaidBy() {
        return paidBy;
    }

    public List<Split> getSplits() {
        return splits;
    }
}
