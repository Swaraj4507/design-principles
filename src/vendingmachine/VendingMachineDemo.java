package vendingmachine;

import vendingmachine.enums.Coin;

public class VendingMachineDemo {
    public static void main(String[] args) {
        VendingMachine vendingMachine = VendingMachine.getInstance();

        // Add products to the inventory
        vendingMachine.addItem("A1", "Coke", 25, 3);
        vendingMachine.addItem("A2", "Pepsi", 25, 2);
        vendingMachine.addItem("B1", "Water", 10, 5);

        // Select a product
        System.out.println("\n--- Step 1: Select an item ---");
        vendingMachine.selectItem("A1");

        // Insert coins
        System.out.println("\n--- Step 2: Insert coins ---");
        vendingMachine.insertCoin(Coin.TEN); // 10
        vendingMachine.insertCoin(Coin.TEN); // 10
        vendingMachine.insertCoin(Coin.FIVE); // 5

        // Dispense the product
        System.out.println("\n--- Step 3: Dispense item ---");
        vendingMachine.dispense(); // Should dispense Coke

        // Select another item
        System.out.println("\n--- Step 4: Select another item ---");
        vendingMachine.selectItem("B1");

        // Insert exact amount
        System.out.println("\n--- Step 5: Insert exact amount ---");
        vendingMachine.insertCoin(Coin.TEN); // 10

        // Dispense the product
        System.out.println("\n--- Step 6: Dispense item ---");
        vendingMachine.dispense();
    }
}