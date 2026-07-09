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
        if (machine.getBalance() == item.getPrice()) {
            machine.getInventory().reduceStock(machine.getSelectedItemCode());
            System.out.println("Dispensed: " + item.getName());
        }
        machine.reset();
        machine.setState(new IdleState(machine));
    }

    // Change-returning version, kept for reference in case exact-payment is dropped
    // in favor of allowing overpayment + making change via VendingMachine.makeChange:
    //
    // @Override
    // public void dispense() {
    //     Item item = machine.getSelectedItem();
    //     int balance = machine.getBalance();
    //     if (balance >= item.getPrice()) {
    //         int changeAmount = balance - item.getPrice();
    //         Optional<Map<Coin, Integer>> change = changeAmount == 0
    //                 ? Optional.of(Map.of())
    //                 : machine.makeChange(changeAmount);
    //
    //         if (change.isPresent()) {
    //             machine.getInventory().reduceStock(machine.getSelectedItemCode());
    //             System.out.println("Dispensed: " + item.getName());
    //             if (changeAmount > 0) {
    //                 System.out.println("Returning change: " + changeAmount + " " + change.get());
    //             }
    //         } else {
    //             System.out.println("Cannot dispense: insufficient change available.");
    //             machine.refundBalance();
    //         }
    //     }
    //     machine.reset();
    //     machine.setState(new IdleState(machine));
    // }

    @Override
    public void refund() {
        System.out.println("Dispensing in progress. Refund not allowed.");
    }
}
