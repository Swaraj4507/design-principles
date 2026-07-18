package notification_system.strategy;

// [NS-8] Unchecked, same convention as shopping_cart's CheckoutException -
//        signals a simulated transient provider/network failure on a
//        single send() attempt (req #5). Dispatcher's retry loop catches
//        this specifically, so a channel throwing anything else (a real
//        bug) still propagates instead of being silently retried.
public class NotificationDeliveryException extends RuntimeException {
    public NotificationDeliveryException(String message) {
        super(message);
    }

    public NotificationDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
