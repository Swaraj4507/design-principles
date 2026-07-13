package split_wise.strategy;

import split_wise.entities.User;

import java.util.List;
import java.util.Map;

public class SplitRequest {
    private final double totalAmount;
    private final List<User> participants;
    private final Map<User, Double> shareData;

    public SplitRequest(double totalAmount, List<User> participants, Map<User, Double> shareData) {
        this.totalAmount = totalAmount;
        this.participants = participants;
        this.shareData = shareData;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public Map<User, Double> getShareData() {
        return shareData;
    }
}
