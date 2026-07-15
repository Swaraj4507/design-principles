package split_wise.entities;

import java.time.Instant;

public interface Transaction {
    String getId();

    Instant getTimestamp();
}
