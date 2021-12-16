package io.agritrack.fruit.state;

import java.util.List;
import java.util.Map;

import io.agritrack.enums.ConsumableType;
import io.agritrack.enums.WarehouseTxState;

public class IncomingRecord {
    public ConsumableType consumableType;

    public String site;

    public List<String> ifcoItems;
    public WarehouseTxState state;

    public Double longitude;
    public Double latitude;
}
