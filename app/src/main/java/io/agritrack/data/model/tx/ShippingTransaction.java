package io.agritrack.data.model.tx;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "shipping_transaction")
public class ShippingTransaction {

    @PrimaryKey
    public Long id;

    @ColumnInfo(name = "timestamp")
    public Long timestamp;

    @ColumnInfo(name = "truck_license_plate")
    public String truckLicensePlate;

    @ColumnInfo(name = "driver_name")
    public String driverName;

    @ColumnInfo(name = "user_name")
    public String user;

    @ColumnInfo(name = "customer")
    public String customer;

    @ColumnInfo(name = "ifco_cnt")
    public Integer ifcoCnt;

    @ColumnInfo(name = "longitude")
    public Double longitude;

    @ColumnInfo(name = "latitude")
    public Double latitude;
}
