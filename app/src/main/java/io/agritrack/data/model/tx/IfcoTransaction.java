package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ifco_transaction")
public class IfcoTransaction {

        @PrimaryKey
        public Long id;

        @ColumnInfo(name = "shipping_tx_id")
        public Long shippingTxId;

        @ColumnInfo(name = "package_tx_id")
        public Long packageTxId;

        @ColumnInfo(name = "storage_tx_id")
        public Long storageTxId;

        @ColumnInfo(name = "barcode")
        public String barcode;
}
