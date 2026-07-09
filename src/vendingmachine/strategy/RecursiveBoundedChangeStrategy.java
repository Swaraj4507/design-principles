package vendingmachine.strategy;

import vendingmachine.enums.Coin;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class RecursiveBoundedChangeStrategy implements ChangeStrategy {

    private static final int INF = 1_000_000;

    private Coin[] coins;
    private int[] available;

    private Integer[][][] memo;
    private boolean[][][] picked;

    @Override
    public Optional<Map<Coin, Integer>> getChange(int amount, Map<Coin, Integer> inventory) {

        coins = Coin.values();

        available = new int[coins.length];
        int maxCount = 0;

        for (int i = 0; i < coins.length; i++) {
            available[i] = inventory.getOrDefault(coins[i], 0);
            maxCount = Math.max(maxCount, available[i]);
        }

        memo = new Integer[coins.length][amount + 1][maxCount + 1];
        picked = new boolean[coins.length][amount + 1][maxCount + 1];

        int ans = solve(0, amount, available[0]);

        if (ans >= INF) {
            return Optional.empty();
        }

        Map<Coin, Integer> breakdown = reconstruct(amount);

        return Optional.of(breakdown);
    }

    private int solve(int idx, int amount, int remaining) {

        if (amount == 0)
            return 0;

        if (idx == coins.length)
            return INF;

        if (memo[idx][amount][remaining] != null)
            return memo[idx][amount][remaining];

        int notPick = solve(
                idx + 1,
                amount,
                idx + 1 < coins.length ? available[idx + 1] : 0
        );

        int pick = INF;

        if (remaining > 0 && amount >= coins[idx].getValue()) {
            pick = 1 + solve(
                    idx,
                    amount - coins[idx].getValue(),
                    remaining - 1
            );
        }

        if (pick < notPick) {
            picked[idx][amount][remaining] = true;
            return memo[idx][amount][remaining] = pick;
        }

        return memo[idx][amount][remaining] = notPick;
    }

    private Map<Coin, Integer> reconstruct(int amount) {

        Map<Coin, Integer> breakdown = new EnumMap<>(Coin.class);

        int idx = 0;
        int remaining = available[0];

        while (amount > 0 && idx < coins.length) {

            if (picked[idx][amount][remaining]) {

                breakdown.merge(coins[idx], 1, Integer::sum);

                amount -= coins[idx].getValue();
                remaining--;

            } else {

                idx++;

                if (idx < coins.length) {
                    remaining = available[idx];
                }
            }
        }

        return breakdown;
    }
}