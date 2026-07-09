package vendingmachine.strategy;

import vendingmachine.enums.Coin;

import java.util.Map;
import java.util.Optional;

public interface ChangeStrategy {
    Optional<Map<Coin, Integer>> getChange(int amount, Map<Coin, Integer> available);
}
