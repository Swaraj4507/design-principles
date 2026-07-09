package vendingmachine.enums;

public enum Coin {
    FIVE(5),
    TEN(10),
    FIFTY(50),
    HUNDRED(100),
    TWO_HUNDRED(200),
    FIVE_HUNDRED(500);

    private final int value;

    Coin(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
