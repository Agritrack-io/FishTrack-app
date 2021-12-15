package io.agritrack.data.model.tx.items;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import io.agritrack.data.model.tx.IfcoTransaction;
import io.agritrack.data.model.tx.StorageTransaction;
import io.agritrack.data.model.tx.TotesTransaction;

public class StorageTxWithItems {

    @Embedded
    public StorageTransaction storageTx;
    @Relation(
            parentColumn = "id",
            entityColumn = "storage_tx_id"
    )
    public List<TotesTransaction> totes;

    @Relation(
            parentColumn = "id",
            entityColumn = "storage_tx_id"
    )
    public List<IfcoTransaction> ifco;
}
