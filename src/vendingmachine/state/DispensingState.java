package vendingmachine.state;

import vendingmachine.entity.Item;
import vendingmachine.enums.Coin;
import vendingmachine.VendingMachine;

public class DispensingState extends VendingMachineState {
    public DispensingState(VendingMachine machine) {
        super(machine);
    }

    @Override
    public void insertCoin(Coin coin) {
        System.out.println("Currently dispensing. Please wait.");
    }

    @Override
    public void selectItem(String code) {
        System.out.println("Currently dispensing. Please wait.");
    }

    @Override
    public void dispense() {
        Item item = machine.getSelectedItem();
        int balance = machine.getBalance();
        if (balance >= item.getPrice()) {
            machine.getInventory().reduceStock(machine.getSelectedItemCode());
            System.out.println("Dispensed: " + item.getName());
            int change = balance - item.getPrice();
            if (change > 0) {
                System.out.println("Returning change: " + change);
            }
        }
        machine.reset();
        machine.setState(new IdleState(machine));
    }

    @Override
    public void refund() {
        System.out.println("Dispensing in progress. Refund not allowed.");
    }
}
