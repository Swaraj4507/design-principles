package vendingmachine.strategy;

import vendingmachine.enums.Coin;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Bounded coin-change DP: finds the breakdown that uses the fewest notes/coins
 * to make exact change, respecting how many of each denomination are actually
 * available. Plain greedy can get stuck picking a large denomination early and
 * fail to find a combination that does exist, so this considers every
 * feasible count of each denomination instead.
 */
public class MinNotesChangeStrategy implements ChangeStrategy {
    private static final int UNREACHABLE = Integer.MAX_VALUE;

    @Override
    public Optional<Map<Coin, Integer>> getChange(int amount, Map<Coin, Integer> available) {
        Coin[] coins = Coin.values();
        int n = coins.length;

        int[][] minNotes = new int[n + 1][amount + 1];
        int[][] countUsed = new int[n + 1][amount + 1];
        for (int[] row : minNotes) {
            Arrays.fill(row, UNREACHABLE);
        }
        minNotes[0][0] = 0;

        for (int i = 1; i <= n; i++) {
            Coin coin = coins[i - 1];
            int value = coin.getValue();
            int maxAvailable = available.getOrDefault(coin, 0);

            for (int a = 0; a <= amount; a++) {
                minNotes[i][a] = minNotes[i - 1][a];
                countUsed[i][a] = 0;

                for (int k = 1; k <= maxAvailable && k * value <= a; k++) {
                    int remaining = minNotes[i - 1][a - k * value];
                    if (remaining != UNREACHABLE && remaining + k < minNotes[i][a]) {
                        minNotes[i][a] = remaining + k;
                        countUsed[i][a] = k;
                    }
                }
            }
        }

        if (minNotes[n][amount] == UNREACHABLE) {
            return Optional.empty();
        }

        Map<Coin, Integer> breakdown = new EnumMap<>(Coin.class);
        int remaining = amount;
        for (int i = n; i >= 1; i--) {
            int k = countUsed[i][remaining];
            if (k > 0) {
                breakdown.put(coins[i - 1], k);
                remaining -= k * coins[i - 1].getValue();
            }
        }
        return Optional.of(breakdown);
    }
}
