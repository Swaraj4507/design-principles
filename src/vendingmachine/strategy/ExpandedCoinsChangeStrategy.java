package vendingmachine.strategy;

import vendingmachine.enums.Coin;

import java.util.*;

/**
 * Expands every physical coin/note into a list and solves it as a
 * classic 0/1 pick-or-not-pick DP.
 *
 * Example:
 * 10 x2, 5 x1, 2 x3
 * becomes
 * [10, 10, 5, 2, 2, 2]
 */
public class ExpandedCoinsChangeStrategy implements ChangeStrategy {

    private static final int INF = 1_000_000;

    private List<Coin> coins;
    private Integer[][] memo;
    private boolean[][] picked;

    @Override
    public Optional<Map<Coin, Integer>> getChange(int amount, Map<Coin, Integer> available) {

        coins = new ArrayList<>();

        for (Coin coin : Coin.values()) {
            int count = available.getOrDefault(coin, 0);
            for (int i = 0; i < count; i++) {
                coins.add(coin);
            }
        }

        memo = new Integer[coins.size() + 1][amount + 1];
        picked = new boolean[coins.size()][amount + 1];

        int ans = solve(0, amount);

        if (ans >= INF) {
            return Optional.empty();
        }

        Map<Coin, Integer> breakdown = new EnumMap<>(Coin.class);

        int idx = 0;
        int remaining = amount;

        while (idx < coins.size() && remaining > 0) {

            if (picked[idx][remaining]) {
                Coin coin = coins.get(idx);
                breakdown.merge(coin, 1, Integer::sum);
                remaining -= coin.getValue();
            }

            idx++;
        }

        return Optional.of(breakdown);
    }

    private int solve(int idx, int amount) {

        if (amount == 0)
            return 0;

        if (idx == coins.size() || amount < 0)
            return INF;

        if (memo[idx][amount] != null)
            return memo[idx][amount];

        int notPick = solve(idx + 1, amount);

        int pick = INF;
        Coin coin = coins.get(idx);

        if (coin.getValue() <= amount) {
            pick = 1 + solve(idx + 1, amount - coin.getValue());
        }

        if (pick < notPick) {
            picked[idx][amount] = true;
            return memo[idx][amount] = pick;
        }

        return memo[idx][amount] = notPick;
    }
}
