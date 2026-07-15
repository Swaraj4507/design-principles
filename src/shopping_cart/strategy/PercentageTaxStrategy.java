package shopping_cart.strategy;

public class PercentageTaxStrategy implements TaxStrategy {
    private final double ratePercentage;

    public PercentageTaxStrategy(double ratePercentage) {
        this.ratePercentage = ratePercentage;
    }

    @Override
    public double calculateTax(double amount) {
        return amount * ratePercentage / 100;
    }
}
