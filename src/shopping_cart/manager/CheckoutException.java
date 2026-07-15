package shopping_cart.manager;

// [13] Unchecked, carrying a human-readable reason: req #8 explicitly asks
//      for checkout to fail "with a clear reason" (insufficient stock for
//      product X, invalid coupon, etc.), which a bare Optional/boolean
//      can't express without a second out-of-band channel.
public class CheckoutException extends RuntimeException {
    public CheckoutException(String message) {
        super(message);
    }
}
