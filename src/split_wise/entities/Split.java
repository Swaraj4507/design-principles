package split_wise.entities;

public class Split {
    private final User user;
    private final double amountOwed;

    public Split(User user, double amountOwed) {
        this.user = user;
        this.amountOwed = amountOwed;
    }

    public User getUser() {
        return user;
    }

    public double getAmountOwed() {
        return amountOwed;
    }
}
