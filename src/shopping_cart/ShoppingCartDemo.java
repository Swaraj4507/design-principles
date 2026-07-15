package shopping_cart;

import shopping_cart.entities.Cart;
import shopping_cart.entities.CartItem;
import shopping_cart.entities.Customer;
import shopping_cart.entities.Inventory;
import shopping_cart.entities.Invoice;
import shopping_cart.entities.Product;
import shopping_cart.manager.CheckoutException;
import shopping_cart.manager.CheckoutManager;
import shopping_cart.strategy.PercentageTaxStrategy;

public class ShoppingCartDemo {
    public static void main(String[] args) {
        System.out.println("--- Step 1: Set up catalog and inventory ---");
        Product laptop = new Product("P1", "Laptop", 800);
        Product mouse = new Product("P2", "Mouse", 20);
        Product keyboard = new Product("P3", "Keyboard", 50);

        Inventory inventory = new Inventory();
        inventory.addStock(laptop, 5);
        inventory.addStock(mouse, 10);
        inventory.addStock(keyboard, 2);

        CheckoutManager checkoutManager = new CheckoutManager(inventory, new PercentageTaxStrategy(8));
        Customer alice = new Customer("C1", "Alice");

        System.out.println("\n--- Step 2: Alice builds a cart ---");
        Cart cart = new Cart(alice);
        cart.addItem(laptop, 1);
        cart.addItem(mouse, 3);
        printCart(cart);

        System.out.println("\n--- Step 3: Update mouse quantity to 0 (should remove the line) ---");
        cart.updateQuantity(mouse.getId(), 0);
        printCart(cart);

        System.out.println("\n--- Step 4: Add mouse back, apply WELCOME10, checkout ---");
        cart.addItem(mouse, 2);
        cart.applyCouponCode("WELCOME10");
        printCart(cart);
        Invoice invoice = checkoutManager.checkout(cart);
        printInvoice(invoice);

        System.out.println("\n--- Step 5: Bob tries to buy more keyboards than are in stock ---");
        Customer bob = new Customer("C2", "Bob");
        Cart bobsCart = new Cart(bob);
        bobsCart.addItem(keyboard, 5);
        try {
            checkoutManager.checkout(bobsCart);
        } catch (CheckoutException e) {
            System.out.println("Checkout failed as expected: " + e.getMessage());
        }

        System.out.println("\n--- Step 6: Alice tries to reuse WELCOME10 (one use per customer) ---");
        Cart aliceAgain = new Cart(alice);
        aliceAgain.addItem(mouse, 1);
        aliceAgain.applyCouponCode("WELCOME10");
        try {
            checkoutManager.checkout(aliceAgain);
        } catch (CheckoutException e) {
            System.out.println("Checkout failed as expected: " + e.getMessage());
        }
    }

    private static void printCart(Cart cart) {
        System.out.println("Cart for " + cart.getCustomer().getName() + ":");
        for (CartItem item : cart.getItems()) {
            System.out.printf("  %-10s qty=%d unitPrice=%.2f lineTotal=%.2f%n",
                    item.getProduct().getName(), item.getQuantity(), item.getProduct().getPrice(), item.lineTotal());
        }
        System.out.printf("  Subtotal: %.2f%n", cart.subtotal());
    }

    private static void printInvoice(Invoice invoice) {
        System.out.println("Invoice for " + invoice.getCustomer().getName() + ":");
        System.out.printf("  Subtotal: %.2f%n", invoice.getSubtotal());
        System.out.printf("  Discount: %.2f%n", invoice.getDiscount());
        System.out.printf("  Tax:      %.2f%n", invoice.getTax());
        System.out.printf("  Total:    %.2f%n", invoice.getTotal());
    }
}
