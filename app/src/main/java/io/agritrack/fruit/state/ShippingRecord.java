package io.agritrack.fruit.state;

import java.util.List;

import io.agritrack.enums.WarehouseTxState;

public class ShippingRecord {

    public String totalWeight;
    public String poleRFID;
    public String harvestLot;
    public Double longitude;
    public Double latitude;
    public String warehouse;
    public List<String> packagedIfco;
    public Integer totalIfcoCnt;
    public WarehouseTxState state;
    public int customerPos = -1;
    public String driverName;
    public String licensePlate;
    public String customer;
    public String driverPhone;
    public String packagingLot;
}
