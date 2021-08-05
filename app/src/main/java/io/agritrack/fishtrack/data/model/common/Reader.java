package io.agritrack.fishtrack.data.model.common;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reader")
public class Reader {

    @PrimaryKey
    private Long id;

    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "serial_number")
    private String serialNumber;

    @ColumnInfo(name = "ip")
    private String ipAddress;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "code")
    private String code;

    @ColumnInfo(name = "date_given")
    private Long givenAt;

    @ColumnInfo(name = "delivery_note")
    private String deliveryNote;

    @ColumnInfo(name = "last_sync")
    private Long lastSync;

  /*  @OneToMany(mappedBy = "reader", fetch = FetchType.LAZY)
    private List<Inventory> inventories;*/
}
