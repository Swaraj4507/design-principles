package shopping_cart.strategy;

public class PercentageDiscountStrategy implements DiscountStrategy {
    private final double percentage;
    private final double maxDiscount;

    public PercentageDiscountStrategy(double percentage, double maxDiscount) {
        this.percentage = percentage;
        this.maxDiscount = maxDiscount;
    }

    // Uncapped percentage off, e.g. "20% off" with no upper limit.
    public PercentageDiscountStrategy(double percentage) {
        this(percentage, Double.MAX_VALUE);
    }

    @Override
    public double calculateDiscount(double amount) {
        double rawDiscount = amount * percentage / 100;
        return Math.min(rawDiscount, Math.min(maxDiscount, amount));
    }
}
