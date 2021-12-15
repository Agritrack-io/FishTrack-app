package io.agritrack.data.model.tx.items;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import io.agritrack.data.model.tx.CollectTransaction;
import io.agritrack.data.model.tx.TotesTransaction;

public class CollectionTxWithItems {

    @Embedded
    public CollectTransaction collectTx;
    @Relation(
            parentColumn = "id",
            entityColumn = "collection_tx_id"
    )
    public List<TotesTransaction> items;
}
