package io.agritrack.fishtrack.data.model.tx;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wh_incoming_transaction")
public class IncomingWHTransaction {
    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "rfid")
    public String rfid;

    @ColumnInfo(name = "state")
    public String state;
}
