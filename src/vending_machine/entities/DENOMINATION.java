package vending_machine.entities;

public enum DENOMINATION {
    FIVE(5),
    TEN(10),
    TWENTY(20),
    FIFTY(50),
    HUNDRED(100),
    TWO_HUNDRED(200),
    FIVE_HUNDRED(500);

    private final int value;

    DENOMINATION(int i ){
        this.value=i;
    }

    int getValue(){
        return value;
    }
}
