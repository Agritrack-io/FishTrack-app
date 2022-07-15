package io.agritrack.fish.state;

import java.util.List;
import java.util.Map;

import io.agritrack.enums.AssetType;
import io.agritrack.enums.ConsumableType;
import io.agritrack.enums.WarehouseTxState;

public class WHTxRecord {
    public long txKey;
    public String assetType;
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
    public String internalItem;
}
