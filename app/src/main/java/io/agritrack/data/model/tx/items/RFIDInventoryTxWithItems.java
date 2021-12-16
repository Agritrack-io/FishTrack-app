package io.agritrack.data.model.tx.items;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import io.agritrack.data.model.tx.IfcoTransaction;
import io.agritrack.data.model.tx.PackageTransaction;
import io.agritrack.data.model.tx.TotesTransaction;
import io.agritrack.data.model.wh.RFIDInventory;

public class RFIDInventoryTxWithItems {

    @Embedded
    public RFIDInventory rfidInventoryTx;
    @Relation(
            parentColumn = "id",
            entityColumn = "inv_tx_id"
    )
    public List<TotesTransaction> totes;
}
