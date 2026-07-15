package shopping_cart.entities;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Cart {
    private final Customer customer;
    // [1] Keyed by productId (not a List<CartItem>) since add/update/remove
    //     (req #2) all look an item up by product, and that should be O(1)
    //     rather than a linear scan. LinkedHashMap keeps insertion order so
    //     the line-item breakdown (req #3) is stable to display.
    private final Map<String, CartItem> items = new LinkedHashMap<>();
    private String appliedCouponCode;

    public Cart(Customer customer) {
        this.customer = customer;
    }

    public Customer getCustomer() {
        return customer;
    }

    // [2] Adding a product already in the cart increases its quantity
    //     instead of creating a second line for it, matching how "add to
    //     cart" behaves for a product you've already added.
    public void addItem(Product product, int quantity) {
        CartItem existing = items.get(product.getId());
        int newQuantity = quantity + (existing != null ? existing.getQuantity() : 0);
        setQuantity(product, newQuantity);
    }

    public void updateQuantity(String productId, int quantity) {
        CartItem item = items.get(productId);
        if (item == null) {
            throw new IllegalArgumentException("Product not in cart: " + productId);
        }
        setQuantity(item.getProduct(), quantity);
    }

    // [8] Shared by addItem/updateQuantity: a quantity dropping to zero (or
    //     below, e.g. via addItem with a negative delta) removes the line
    //     instead of leaving a dead zero-quantity CartItem sitting in the
    //     cart, which req #2's "remove" and #3's breakdown both implicitly
    //     assume can't happen.
    private void setQuantity(Product product, int quantity) {
        if (quantity <= 0) {
            items.remove(product.getId());
            return;
        }
        CartItem existing = items.get(product.getId());
        if (existing != null) {
            existing.setQuantity(quantity);
        } else {
            items.put(product.getId(), new CartItem(product, quantity));
        }
    }

    public void removeItem(String productId) {
        items.remove(productId);
    }

    public Collection<CartItem> getItems() {
        return items.values();
    }

    public double subtotal() {
        double total = 0;
        for (CartItem item : items.values()) {
            total += item.lineTotal();
        }
        return total;
    }

    public String getAppliedCouponCode() {
        return appliedCouponCode;
    }

    public void applyCouponCode(String code) {
        this.appliedCouponCode = code;
    }
}
