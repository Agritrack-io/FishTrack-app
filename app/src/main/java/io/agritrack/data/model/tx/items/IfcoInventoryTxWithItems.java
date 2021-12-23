package io.agritrack.data.model.tx.items;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import io.agritrack.data.model.tx.IfcoTransaction;
import io.agritrack.data.model.wh.CoInventory;
import io.agritrack.data.model.wh.RFIDInventory;

public class IfcoInventoryTxWithItems {

    @Embedded
    public CoInventory coInventoryTx;
    @Relation(
            parentColumn = "id",
            entityColumn = "inv_tx_id"
    )
    public List<IfcoTransaction> ifco;
}
