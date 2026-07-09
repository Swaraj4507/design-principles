package vendingmachine.entity;

import vendingmachine.enums.Coin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CashInventory {
    private final Map<Coin, Integer> stock = new ConcurrentHashMap<>();

    public void addCash(Coin coin, int count) {
        stock.merge(coin, count, Integer::sum);
    }

    public Map<Coin, Integer> getAvailable() {
        return Map.copyOf(stock);
    }

    public void deduct(Map<Coin, Integer> used) {
        used.forEach((coin, count) -> stock.computeIfPresent(coin, (k, v) -> v - count));
    }
}
