package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "totes_transaction")
public class TotesTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "collection_tx_id")
    public Long collectionTxId;

    @ColumnInfo(name = "package_tx_id")
    public Long packageTxId;

    @ColumnInfo(name = "storage_tx_id")
    public Long storageTxId;

    @ColumnInfo(name = "epc")
    public String epc;
}
