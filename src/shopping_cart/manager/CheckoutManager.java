package shopping_cart.manager;

import shopping_cart.entities.Cart;
import shopping_cart.entities.CartItem;
import shopping_cart.entities.Coupon;
import shopping_cart.entities.Inventory;
import shopping_cart.entities.InventoryItem;
import shopping_cart.entities.Invoice;
import shopping_cart.strategy.FlatDiscountStrategy;
import shopping_cart.strategy.PercentageDiscountStrategy;
import shopping_cart.strategy.TaxStrategy;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CheckoutManager {
    private final Inventory inventory;
    private final TaxStrategy taxStrategy;
    private final Map<String, Coupon> couponsByCode = new ConcurrentHashMap<>();

    public CheckoutManager(Inventory inventory, TaxStrategy taxStrategy) {
        this.inventory = inventory;
        this.taxStrategy = taxStrategy;
        registerDefaultCoupons();
    }

    public void addCoupon(Coupon coupon) {
        couponsByCode.put(coupon.getCode(), coupon);
    }

    // [18] A couple of coupons seeded by default so the package is
    //      runnable/testable without extra setup code elsewhere. Each is
    //      just a Coupon wired to a configured DiscountStrategy instance,
    //      same as how any other coupon would be created (e.g. from an
    //      admin API) — nothing special about "default" here. WELCOME10
    //      shows unlimited total supply capped at one use per customer;
    //      FLAT20 shows a limited total supply (first 100 redemptions).
    private void registerDefaultCoupons() {
        Instant oneYearOut = Instant.now().plus(Duration.ofDays(365));
        addCoupon(new Coupon("WELCOME10", oneYearOut, 0, new PercentageDiscountStrategy(10, 50),
                1, Integer.MAX_VALUE));
        addCoupon(new Coupon("FLAT20", oneYearOut, 100, new FlatDiscountStrategy(20),
                1, 100));
    }

    // [14] Coupon eligibility (expiry, minimum cart value) is checked
    //      before any inventory reservation is attempted — it's a cheap,
    //      read-only, side-effect-free filter, so failing fast here means
    //      a checkout that was always going to fail never reserves stock
    //      only to have to roll it back. Usage-cap enforcement is *not*
    //      part of this check — see [20] for why that has to happen later.
    public Invoice checkout(Cart cart) {
        Optional<Coupon> coupon = resolveCoupon(cart);

        // [15] Reserves items one at a time and unwinds everything reserved
        //      so far the moment one fails, rather than reserving whatever
        //      is available and confirming partial quantities. That's req
        //      #8's "atomic w.r.t. inventory" — implemented as unwind-on-
        //      failure rather than a two-phase lock across all items, since
        //      each InventoryItem.reserve() is already safe on its own; the
        //      orchestrator only has to handle "some succeeded, one failed."
        List<CartItem> reserved = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            InventoryItem inventoryItem = inventory.getItem(item.getProduct().getId());
            if (inventoryItem == null || !inventoryItem.reserve(item.getQuantity())) {
                rollback(reserved);
                throw new CheckoutException("Insufficient stock for product: " + item.getProduct().getId());
            }
            reserved.add(item);
        }

        // [20] Redemption is attempted here — after inventory is reserved
        //      but before it's confirmed — so losing the race on the
        //      coupon's total-redemption cap can still roll back cleanly
        //      via release(). Attempting it earlier (in resolveCoupon,
        //      before reservation) couldn't fail this late and stay
        //      correct under concurrency; attempting it after confirm()
        //      would mean unwinding an already-sold reservation instead of
        //      one that's still just held.
        double subtotal = cart.subtotal();
        double discount = 0;
        if (coupon.isPresent()) {
            if (!coupon.get().tryRedeem(cart.getCustomer())) {
                rollback(reserved);
                throw new CheckoutException("Coupon redemption limit reached: " + coupon.get().getCode());
            }
            discount = coupon.get().getDiscountStrategy().calculateDiscount(subtotal);
        }
        double afterDiscount = subtotal - discount;
        double tax = taxStrategy.calculateTax(afterDiscount);
        double total = afterDiscount + tax;

        // [16] Confirm only runs after every item's reservation succeeded,
        //      the coupon (if any) was successfully redeemed, and pricing
        //      was computed — this is the single point checkout is
        //      actually committed; everything before it is reversible,
        //      nothing after it needs to be.
        for (CartItem item : reserved) {
            inventory.getItem(item.getProduct().getId()).confirm(item.getQuantity());
        }

        return new Invoice(cart.getCustomer(), new ArrayList<>(cart.getItems()), subtotal, discount, tax, total);
    }

    private Optional<Coupon> resolveCoupon(Cart cart) {
        String code = cart.getAppliedCouponCode();
        if (code == null) {
            return Optional.empty();
        }
        Coupon coupon = couponsByCode.get(code);
        if (coupon == null || !coupon.isEligible(cart)) {
            throw new CheckoutException("Invalid or ineligible coupon code: " + code);
        }
        return Optional.of(coupon);
    }

    private void rollback(List<CartItem> reserved) {
        for (CartItem item : reserved) {
            inventory.getItem(item.getProduct().getId()).release(item.getQuantity());
        }
    }
}
