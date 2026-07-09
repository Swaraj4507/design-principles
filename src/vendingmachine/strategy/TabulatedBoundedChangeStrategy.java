package vendingmachine.strategy;

import vendingmachine.enums.Coin;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class TabulatedBoundedChangeStrategy implements ChangeStrategy {

    private static final int INF = 1_000_000;

    @Override
    public Optional<Map<Coin, Integer>> getChange(int amount, Map<Coin, Integer> inventory) {
        Coin[] coins = Coin.values();
        int n = coins.length;

        int[] available = new int[n];
        int maxCount = 0;
        for (int i = 0; i < n; i++) {
            available[i] = inventory.getOrDefault(coins[i], 0);
            maxCount = Math.max(maxCount, available[i]);
        }

        int[][][] dp = new int[n + 1][amount + 1][maxCount + 1];
        boolean[][][] picked = new boolean[n + 1][amount + 1][maxCount + 1];

        // Initialize base cases
        for (int i = 0; i <= n; i++) {
            for (int a = 0; a <= amount; a++) {
                for (int r = 0; r <= maxCount; r++) {
                    if (a == 0) {
                        dp[i][a][r] = 0;
                    } else if (i == n) {
                        dp[i][a][r] = INF;
                    } else {
                        dp[i][a][r] = INF;
                    }
                }
            }
        }

        // Fill DP table
        for (int i = n - 1; i >= 0; i--) {
            int value = coins[i].getValue();
            for (int a = 1; a <= amount; a++) {
                for (int r = 0; r <= available[i]; r++) {
                    int notPick = dp[i + 1][a][(i + 1 < n) ? available[i + 1] : 0];
                    int pick = INF;
                    if (r > 0 && a >= value) {
                        pick = 1 + dp[i][a - value][r - 1];
                    }

                    if (pick < notPick) {
                        dp[i][a][r] = pick;
                        picked[i][a][r] = true;
                    } else {
                        dp[i][a][r] = notPick;
                        picked[i][a][r] = false;
                    }
                }
            }
        }

        if (dp[0][amount][available[0]] >= INF) {
            return Optional.empty();
        }

        // Reconstruct using picked array
        Map<Coin, Integer> breakdown = new EnumMap<>(Coin.class);
        int i = 0, a = amount, r = available[0];
        while (a > 0 && i < n) {
            if (picked[i][a][r]) {
                breakdown.merge(coins[i], 1, Integer::sum);
                a -= coins[i].getValue();
                r--;
            } else {
                i++;
                if (i < n) r = available[i];
            }
        }

        return Optional.of(breakdown);
    }
}
