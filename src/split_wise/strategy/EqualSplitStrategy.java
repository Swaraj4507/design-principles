package split_wise.strategy;

import split_wise.entities.Split;
import split_wise.entities.User;

import java.util.ArrayList;
import java.util.List;

public class EqualSplitStrategy implements SplitStrategy {

    @Override
    public List<Split> calculateSplit(SplitRequest request) {
        List<User> participants = request.getParticipants();
        double share = request.getTotalAmount() / participants.size();

        List<Split> splits = new ArrayList<>();
        for (User user : participants) {
            splits.add(new Split(user, share));
        }
        return splits;
    }
}
