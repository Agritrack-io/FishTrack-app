package io.agritrack.fishtrack.state;

import java.util.List;
import java.util.Map;

import io.agritrack.fishtrack.enums.AssetType;
import io.agritrack.fishtrack.enums.ConsumableType;
import io.agritrack.fishtrack.enums.WarehouseTxState;

public class WHTxRecord {

    public AssetType assetType;
    public ConsumableType consumableType;
    public String incomingItemType, selectedToggleButtonTo, selectedToggleButtonFrom;
    public String outgoingItemType;
    public String from = null;
    public String to = null;
    public String site;

    public int assetTypePos = -1;
    public Map<String, List<String>> items;
    public Map<String, Integer> barcodeItems;
    public WarehouseTxState state;

    public Double longitude;
    public Double latitude;
}
