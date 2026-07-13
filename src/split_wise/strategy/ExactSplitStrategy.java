package split_wise.strategy;

import split_wise.entities.Split;
import split_wise.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExactSplitStrategy implements SplitStrategy {
    private static final double EPSILON = 0.01;

    @Override
    public List<Split> calculateSplit(SplitRequest request) {
        Map<User, Double> shareData = request.getShareData();

        double sum = shareData.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(sum - request.getTotalAmount()) > EPSILON) {
            throw new IllegalArgumentException("Exact amounts must sum to the total expense amount");
        }

        List<Split> splits = new ArrayList<>();
        for (User user : request.getParticipants()) {
            splits.add(new Split(user, shareData.get(user)));
        }
        return splits;
    }
}
