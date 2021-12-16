package io.agritrack.data.model.tx.items;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import io.agritrack.data.model.tx.ConsumableTransaction;
import io.agritrack.data.model.tx.IfcoTransaction;
import io.agritrack.data.model.tx.ShippingTransaction;

public class IncomingTxWithItems {

    @Embedded
    public ConsumableTransaction incomingTx;
    @Relation(
            parentColumn = "id",
            entityColumn = "incoming_tx_id"
    )
    public List<IfcoTransaction> ifco;
}
