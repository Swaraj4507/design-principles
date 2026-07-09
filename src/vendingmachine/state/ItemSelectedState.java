package vendingmachine.state;

import vendingmachine.enums.Coin;
import vendingmachine.VendingMachine;

public class ItemSelectedState extends VendingMachineState {
    public ItemSelectedState(VendingMachine machine) {
        super(machine);
    }

    @Override
    public void insertCoin(Coin coin) {
        int price = machine.getSelectedItem().getPrice();
        int remaining = price - machine.getBalance();
        if (coin.getValue() > remaining) {
            System.out.println("Coin rejected: please insert the exact amount. Remaining: " + remaining);
            return;
        }

        machine.addBalance(coin);
        System.out.println("Coin Inserted: " + coin.getValue());
        if (machine.getBalance() == price) {
            System.out.println("Exact amount received.");
            machine.setState(new HasMoneyState(machine));
        }
    }

    @Override
    public void selectItem(String code) {
        System.out.println("Item already selected.");
    }

    @Override
    public void dispense() {
        System.out.println("Please insert sufficient money.");
    }

    @Override
    public void refund() {
        machine.refundBalance();
        machine.reset();
        machine.setState(new IdleState(machine));
    }
}
