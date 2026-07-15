package shopping_cart.entities;

public class InventoryItem {
    private final Product product;
    private int availableQuantity;
    private int reservedQuantity;

    public InventoryItem(Product product, int availableQuantity) {
        this.product = product;
        this.availableQuantity = availableQuantity;
    }

    public Product getProduct() {
        return product;
    }

    public synchronized int getAvailableQuantity() {
        return availableQuantity;
    }

    public synchronized int getReservedQuantity() {
        return reservedQuantity;
    }

    // [9] synchronized + re-checking availableQuantity here (not trusting
    //     whatever the caller already saw when it decided to check out) is
    //     the actual guard against overselling under concurrent checkout
    //     (req #10) — two threads racing for the last unit can't both
    //     succeed. Same reasoning as Locker.assign().
    public synchronized boolean reserve(int quantity) {
        if (quantity > availableQuantity) {
            return false;
        }
        availableQuantity -= quantity;
        reservedQuantity += quantity;
        return true;
    }

    // [10] Confirms a reservation as sold. The units already left
    //      availableQuantity at reserve() time, so confirming only needs
    //      to drop them from reservedQuantity (req #9 — decrement reserved
    //      inventory on successful checkout).
    public synchronized void confirm(int quantity) {
        reservedQuantity -= quantity;
    }

    // [11] Mirrors reserve(): rolls a reservation back to available stock.
    //      Used when a later item in the same checkout fails validation, so
    //      a failed checkout never leaves earlier items' stock stuck in
    //      reservedQuantity limbo (req #8 — all-or-nothing checkout).
    public synchronized void release(int quantity) {
        reservedQuantity -= quantity;
        availableQuantity += quantity;
    }
}
