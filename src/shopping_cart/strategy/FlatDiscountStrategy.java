package shopping_cart.strategy;

public class FlatDiscountStrategy implements DiscountStrategy {
    private final double amountOff;

    public FlatDiscountStrategy(double amountOff) {
        this.amountOff = amountOff;
    }

    // [17] Clamped to the amount itself: a discount can never exceed what
    //      it's being applied to, otherwise the post-discount subtotal in
    //      CheckoutManager would go negative. This is the strategy's own
    //      invariant to enforce since it owns the computation.
    @Override
    public double calculateDiscount(double amount) {
        return Math.min(amountOff, amount);
    }
}
