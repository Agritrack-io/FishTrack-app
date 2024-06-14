package io.agritrack.philosofish.enums;

public enum WarehouseTxState {
    OrderSend(2), OrderReceived(3), Outgoing(4), Incoming(5), InventoryExecuted(6), ToBeSent(7), ToBeReceived(8), Internal(9);

    private final int val;

    WarehouseTxState(int state) {
        this.val = state;
    }

    public int getValue() {
        return this.val;
    }
}
