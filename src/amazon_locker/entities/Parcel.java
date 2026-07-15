package amazon_locker.entities;

// [3] Kept separate from Locker on purpose: recipient/order info belongs to
//     the delivery domain, not the locker hardware. Locker only ever needs
//     a Parcel reference + its size, so the two can evolve independently.
public class Parcel {
    private final String id;
    private final String recipientName;
    private final String recipientContact;
    private final Size size;

    public Parcel(String id, String recipientName, String recipientContact, Size size) {
        this.id = id;
        this.recipientName = recipientName;
        this.recipientContact = recipientContact;
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getRecipientContact() {
        return recipientContact;
    }

    public Size getSize() {
        return size;
    }
}
