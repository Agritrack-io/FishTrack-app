package io.agritrack.data.model.tx.items;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import io.agritrack.data.model.tx.IfcoTransaction;
import io.agritrack.data.model.tx.PackageTransaction;
import io.agritrack.data.model.tx.TotesTransaction;

public class PackageTxWithItems {

    @Embedded
    public PackageTransaction packageTx;
    @Relation(
            parentColumn = "id",
            entityColumn = "package_tx_id"
    )
    public List<TotesTransaction> totes;

    @Relation(
            parentColumn = "id",
            entityColumn = "package_tx_id"
    )
    public List<IfcoTransaction> ifco;
}
