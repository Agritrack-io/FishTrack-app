package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import io.agritrack.data.converter.ConsumableTypeConverter;

@Entity(tableName = "consumable_transaction")
public class ConsumableTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "timestamp")
    public Long timestamp;

    @ColumnInfo(name = "barcode")
    public String barcode;

    @ColumnInfo(name = "quantity")
    public Integer quantity;

    @TypeConverters(ConsumableTypeConverter.class)
    @ColumnInfo(name = "consumable_type")
    public String consumableType;

    @ColumnInfo(name = "current_site")
    public String site;

    @ColumnInfo(name = "ftes")
    public String ftes;

    @ColumnInfo(name = "dispatch_note")
    public String dispatchNote;

    @ColumnInfo(name = "state")
    public String state;

    @ColumnInfo(name = "collection_lot")
    public String collectionLot;

    @ColumnInfo(name = "source")
    public String from;

    @ColumnInfo(name = "dest")
    public String to;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;
}
