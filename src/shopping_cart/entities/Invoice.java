package shopping_cart.entities;

import java.time.Instant;
import java.util.List;

public class Invoice {
    private final Customer customer;
    private final List<CartItem> items;
    private final double subtotal;
    private final double discount;
    private final double tax;
    private final double total;
    private final Instant issuedAt;

    // [6] Takes a defensive copy of the cart's items at issue time, so a
    //     later change to the live Cart (or its reuse for a new order)
    //     can't retroactively alter a receipt that's already been issued.
    public Invoice(Customer customer, List<CartItem> items, double subtotal, double discount, double tax, double total) {
        this.customer = customer;
        this.items = List.copyOf(items);
        this.subtotal = subtotal;
        this.discount = discount;
        this.tax = tax;
        this.total = total;
        this.issuedAt = Instant.now();
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDiscount() {
        return discount;
    }

    public double getTax() {
        return tax;
    }

    public double getTotal() {
        return total;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }
}
