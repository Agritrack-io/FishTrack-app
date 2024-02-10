package io.agritrack.kefalonia.data.model.common;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reader")
public class Reader {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "serial_number")
    public String serialNumber;

    @ColumnInfo(name = "ip")
    public String ipAddress;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "code")
    public String code;

    @ColumnInfo(name = "date_given")
    public Long givenAt;

    @ColumnInfo(name = "delivery_note")
    public String deliveryNote;

    @ColumnInfo(name = "last_sync")
    public Long lastSync;

  /*  @OneToMany(mappedBy = "reader", fetch = FetchType.LAZY)
    public List<Inventory> inventories;*/
}
