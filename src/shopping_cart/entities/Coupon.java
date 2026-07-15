package shopping_cart.entities;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import shopping_cart.strategy.DiscountStrategy;

public class Coupon {
    private final String code;
    private final Instant expiryAt;
    private final double minCartValue;
    private final DiscountStrategy discountStrategy;
    private final int maxUsesPerCustomer;
    private final int maxTotalRedemptions;
    // [5] Tracked per-customer on the coupon itself rather than a separate
    //     redemption ledger: usage is always checked scoped to a single
    //     coupon, which is exactly what a Map keyed by customerId gives
    //     you in O(1). Nothing in the requirements needs a cross-coupon
    //     redemption history.
    private final Map<String, Integer> usageCountByCustomer = new HashMap<>();
    private int totalRedemptions;

    public Coupon(String code, Instant expiryAt, double minCartValue, DiscountStrategy discountStrategy,
                   int maxUsesPerCustomer, int maxTotalRedemptions) {
        this.code = code;
        this.expiryAt = expiryAt;
        this.minCartValue = minCartValue;
        this.discountStrategy = discountStrategy;
        this.maxUsesPerCustomer = maxUsesPerCustomer;
        this.maxTotalRedemptions = maxTotalRedemptions;
    }

    public String getCode() {
        return code;
    }

    public DiscountStrategy getDiscountStrategy() {
        return discountStrategy;
    }

    // [4] Cheap, side-effect-free checks only (expiry, minimum cart value).
    //     Deliberately excludes the usage caps below — those can only be
    //     answered truthfully at the moment of redemption (see tryRedeem)
    //     — so this exists purely as a fail-fast filter CheckoutManager can
    //     run before it bothers reserving inventory for a coupon that's
    //     plainly expired or below the minimum.
    public boolean isEligible(Cart cart) {
        if (Instant.now().isAfter(expiryAt)) {
            return false;
        }
        return cart.subtotal() >= minCartValue;
    }

    // [19] synchronized check-and-increment, same shape as
    //      InventoryItem.reserve(): once a coupon has a total-redemption
    //      cap it's a resource shared across concurrent checkouts, not
    //      just a per-customer flag. Splitting this into a separate check
    //      and increment would let two concurrent checkouts both pass the
    //      cap check and both claim the same last redemption slot, so both
    //      have to happen atomically here.
    public synchronized boolean tryRedeem(Customer customer) {
        if (totalRedemptions >= maxTotalRedemptions) {
            return false;
        }
        int uses = usageCountByCustomer.getOrDefault(customer.getId(), 0);
        if (uses >= maxUsesPerCustomer) {
            return false;
        }
        usageCountByCustomer.put(customer.getId(), uses + 1);
        totalRedemptions++;
        return true;
    }
}
