package shopping_cart.strategy;

// [7] Returns the discount/tax amount itself, not the adjusted total, so
//     Invoice can show subtotal/discount/tax/total as separate line items
//     (req #3, #9) instead of reverse-engineering them from one combined
//     figure.
public interface DiscountStrategy {
    double calculateDiscount(double amount);
}
