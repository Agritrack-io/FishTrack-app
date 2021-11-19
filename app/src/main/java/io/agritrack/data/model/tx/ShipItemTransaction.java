package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ship_item_tx")
public class ShipItemTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "transport_id")
    public Long transportTxId;

    @ColumnInfo(name = "receipt_id")
    public Long receiptTxId;

    @ColumnInfo(name = "package_id")
    public Long packageTxId;

    @ColumnInfo(name = "lot") // links box to transportTx/packagingTx
    public String lot;

    @ColumnInfo(name = "bin_rfId")
    public String binRFID;

    @ColumnInfo(name = "delivered")
    public Boolean delivered;
}
