package split_wise.strategy;

import split_wise.entities.Split;
import split_wise.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PercentSplitStrategy implements SplitStrategy {
    private static final double EPSILON = 0.01;

    @Override
    public List<Split> calculateSplit(SplitRequest request) {
        Map<User, Double> shareData = request.getShareData();

        double sum = shareData.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(sum - 100.0) > EPSILON) {
            throw new IllegalArgumentException("Percentages must sum to 100");
        }

        List<Split> splits = new ArrayList<>();
        for (User user : request.getParticipants()) {
            double percentage = shareData.get(user);
            splits.add(new Split(user, request.getTotalAmount() * percentage / 100.0));
        }
        return splits;
    }
}
