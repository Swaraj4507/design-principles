# Designing Shopping Cart / Checkout System

## Requirements
1. A customer should be able to add an item to their cart, specifying a quantity.
2. A customer should be able to update the quantity of an item already in the cart, or remove it entirely.
3. The cart should show a line-item breakdown (item, quantity, unit price, line total) and a running subtotal.
4. At checkout, the system should re-validate inventory availability for every item in the cart before allowing payment, since stock may have changed since items were added.
5. The customer should be able to apply a coupon/promo code at checkout; the system should validate the code (exists, not expired, not already used by this customer, minimum-cart-value met) before applying it.
6. The system should support multiple discount types (flat amount off, percentage off, e.g. "10% off up to $50") applied during price calculation.
7. The system should compute tax on the post-discount amount to produce a final payable total.
8. Checkout should be atomic with respect to inventory: if any item fails availability re-validation, checkout should fail for the whole cart with a clear reason, rather than partially reserving stock.
9. On successful checkout, the system should decrement reserved inventory and produce an order/receipt with the final price breakdown (subtotal, discount, tax, total).
10. The system should handle concurrent checkouts against the same inventory without overselling (two customers checking out the last unit of an item shouldn't both succeed).
11. The system should be extensible to new discount types and new tax rules without changing the core cart/checkout flow.

## Out of scope (for now)
- Actual payment processing / integration with a payment gateway (assume payment always succeeds once checkout is authorized).
- Multi-currency support.
- Shipping cost calculation and delivery address handling.
- Product catalog/search (assume items already exist with a known id, price, and stock level).
