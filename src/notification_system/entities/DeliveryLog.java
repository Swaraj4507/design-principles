package notification_system.entities;

import java.time.Instant;

public class DeliveryLog {
    private final String id;
    private final ChannelType channelType;
    private final DeliveryStatus status;
    private final Instant timestamp;

    public DeliveryLog(String id, ChannelType channelType, DeliveryStatus status, Instant timestamp) {
        this.id=id;
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
