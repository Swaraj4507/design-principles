package split_wise.strategy;

import split_wise.entities.Split;

import java.util.List;

public interface SplitStrategy {
    List<Split> calculateSplit(SplitRequest request);
}
