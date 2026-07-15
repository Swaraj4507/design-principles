package shopping_cart.entities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// [12] Container + identity only, mirroring LockerStation/Locker: no
//      reserve/confirm/release logic here, that stays on InventoryItem
//      which owns the counters it's guarding. ConcurrentHashMap so
//      addStock/getItem are themselves safe under concurrent access,
//      independent of the per-item synchronized methods.
public class Inventory {
    private final Map<String, InventoryItem> itemsByProductId = new ConcurrentHashMap<>();

    public void addStock(Product product, int quantity) {
        itemsByProductId.put(product.getId(), new InventoryItem(product, quantity));
    }

    public InventoryItem getItem(String productId) {
        return itemsByProductId.get(productId);
    }
}
