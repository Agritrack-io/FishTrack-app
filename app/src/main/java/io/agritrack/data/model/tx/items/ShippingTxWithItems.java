package io.agritrack.data.model.tx.items;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import io.agritrack.data.model.tx.IfcoTransaction;
import io.agritrack.data.model.tx.ShippingTransaction;

public class ShippingTxWithItems {

    @Embedded
    public ShippingTransaction shippingTx;

    @Relation(
            parentColumn = "id",
            entityColumn = "shipping_tx_id"
    )
    public List<IfcoTransaction> ifco;
}
