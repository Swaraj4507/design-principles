package vendingmachine.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Inventory {
    private final Map<String, Item> itemMap = new ConcurrentHashMap<>();
    private final Map<String, Integer> stockMap = new ConcurrentHashMap<>();

    public void addItem(String code, Item item, int quantity) {
        itemMap.put(code, item);
        stockMap.put(code, quantity);
    }

    public Item getItem(String code) {
        return itemMap.get(code);
    }

    public boolean isAvailable(String code) {
        return stockMap.getOrDefault(code, 0) > 0;
    }

    public void reduceStock(String code) {
        stockMap.computeIfPresent(code, (k, quantity) -> quantity - 1);
    }
}