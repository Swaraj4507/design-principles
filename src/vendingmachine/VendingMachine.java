package vendingmachine;

import vendingmachine.entity.Inventory;
import vendingmachine.entity.Item;
import vendingmachine.enums.Coin;
import vendingmachine.state.*;

public class VendingMachine {
    private final static VendingMachine INSTANCE = new VendingMachine();
    private final Inventory inventory = new Inventory();
    private VendingMachineState currentVendingMachineState;
    private int balance = 0;
    private String selectedItemCode;

    private VendingMachine() {
        currentVendingMachineState = new IdleState(this);
    }

    public static VendingMachine getInstance() {
        return INSTANCE;
    }

    public synchronized void insertCoin(Coin coin) {
        currentVendingMachineState.insertCoin(coin);
    }

    public synchronized Item addItem(String code, String name, int price, int quantity) {
        Item item = new Item(code, name, price);
        inventory.addItem(code, item, quantity);
        return item;
    }

    public synchronized void selectItem(String code) {
        currentVendingMachineState.selectItem(code);
    }

    public synchronized void dispense() {
        currentVendingMachineState.dispense();
    }

    public synchronized void refundBalance() {
        System.out.println("Refunding: " + balance);
        balance = 0;
    }

    public synchronized void reset() {
        selectedItemCode = null;
        balance = 0;
    }

    public void addBalance(int value) {
        balance += value;
    }

    public Item getSelectedItem() {
        return inventory.getItem(selectedItemCode);
    }

    public String getSelectedItemCode() {
        return selectedItemCode;
    }

    public void setSelectedItemCode(String code) {
        this.selectedItemCode = code;
    }

    public void setState(VendingMachineState vendingMachineState) {
        this.currentVendingMachineState = vendingMachineState;
    }

    // Getters for states and inventory
    public Inventory getInventory() { return inventory; }
    public int getBalance() { return balance; }
}
