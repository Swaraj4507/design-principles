package vending_machine.entities;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Inventory {

    // itemMap: code → Item metadata
    private final Map<String, Item> itemMap = new ConcurrentHashMap<>();

    // stockMap: code → quantity available
    private final Map<String, Integer> stockMap = new ConcurrentHashMap<>();

    public boolean addItem(Item item, int quantity) {
        if (item == null || item.getCode() == null) return false;
        itemMap.putIfAbsent(item.getCode(), item);
        stockMap.merge(item.getCode(), quantity, Integer::sum);
        return true;
    }

    public boolean removeItem(String code) {
        if (!itemMap.containsKey(code)) return false;
        itemMap.remove(code);
        stockMap.remove(code);
        return true;
    }

    public boolean addStock(String code, int quantity) {
        if (!itemMap.containsKey(code)) return false;
        stockMap.merge(code, quantity, Integer::sum);
        return true;
    }

    public boolean decreaseStock(String code, int quantity) {
        if (!itemMap.containsKey(code)) return false;
        int currentQty = stockMap.getOrDefault(code, 0);
        if (currentQty < quantity) return false;

        int newQty = currentQty - quantity;
        if (newQty == 0) {
            itemMap.remove(code);
            stockMap.remove(code);
        } else {
            stockMap.put(code, newQty);
        }
        return true;
    }

    public boolean isAvailable(String code) {
        return stockMap.getOrDefault(code, 0) > 0;
    }

    public int getStock(String code) {
        return stockMap.getOrDefault(code, 0);
    }

    public Optional<Item> getItem(String code) {
        if(!itemMap.containsKey(code)) return Optional.empty();
        return Optional.of(itemMap.get(code));
    }
}

