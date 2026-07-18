package notification_system.entities;

import java.time.Instant;

public class DeliveryLog {
    private final ChannelType channelType;
    private final DeliveryStatus status;
    private final Instant timestamp;

    public DeliveryLog(ChannelType channelType, DeliveryStatus status, Instant timestamp) {
        this.channelType = channelType;
        this.status = status;
        this.timestamp = timestamp;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
