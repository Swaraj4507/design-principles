package shopping_cart.entities;

public class CartItem {
    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    // [3] Package-private on purpose: Cart is the only class allowed to
    //     mutate an item's quantity, so every change goes through
    //     Cart.addItem/updateQuantity/removeItem instead of external code
    //     grabbing a CartItem out of getItems() and mutating it directly.
    void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double lineTotal() {
        return product.getPrice() * quantity;
    }
}
