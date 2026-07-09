package vendingmachine;

import vendingmachine.entity.CashInventory;
import vendingmachine.entity.Inventory;
import vendingmachine.entity.Item;
import vendingmachine.enums.Coin;
import vendingmachine.state.*;
// import vendingmachine.strategy.ChangeStrategy;
// import vendingmachine.strategy.MinNotesChangeStrategy;

import java.util.HashMap;
import java.util.Map;
// import java.util.Optional;

public class VendingMachine {
    private final static VendingMachine INSTANCE = new VendingMachine();
    private final Inventory inventory = new Inventory();
    private final CashInventory cashInventory = new CashInventory();
    // private final ChangeStrategy changeStrategy = new MinNotesChangeStrategy();
    private final Map<Coin, Integer> insertedCoins = new HashMap<>();
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

    public synchronized void addCash(Coin coin, int count) {
        cashInventory.addCash(coin, count);
    }

    // Bounded change-making via ChangeStrategy, kept for reference -- currently the
    // machine requires exact payment instead (see ItemSelectedState.insertCoin).
    // public Optional<Map<Coin, Integer>> makeChange(int amount) {
    //     Optional<Map<Coin, Integer>> change = changeStrategy.getChange(amount, cashInventory.getAvailable());
    //     change.ifPresent(cashInventory::deduct);
    //     return change;
    // }

    public synchronized void selectItem(String code) {
        currentVendingMachineState.selectItem(code);
    }

    public synchronized void dispense() {
        currentVendingMachineState.dispense();
    }

    public synchronized void refundBalance() {
        System.out.println("Refunding: " + balance + " " + insertedCoins);
        cashInventory.deduct(insertedCoins);
        insertedCoins.clear();
        balance = 0;
    }

    public synchronized void reset() {
        selectedItemCode = null;
        insertedCoins.clear();
        balance = 0;
    }

    public void addBalance(Coin coin) {
        balance += coin.getValue();
        insertedCoins.merge(coin, 1, Integer::sum);
        cashInventory.addCash(coin, 1);
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
    public Map<Coin, Integer> getCashOnHand() { return cashInventory.getAvailable(); }
    public int getBalance() { return balance; }
}
