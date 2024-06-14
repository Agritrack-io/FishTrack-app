package io.agritrack.philosofish.fish.state;

import java.util.List;
import java.util.Map;

import io.agritrack.philosofish.enums.ConsumableType;
import io.agritrack.philosofish.enums.WarehouseTxState;

public class WHTxRecord {
    public long txKey;
    public String assetType;
    public ConsumableType consumableType;
    public String incomingItemType, selectedToggleButtonTo, selectedToggleButtonFrom;
    public String outgoingItemType;
    public String fromSite = null;
    public String toSite = null;
    public String fromAsset = null;
    public String toAsset = null;
    public String site;

    public int assetTypePos = -1;
    public Map<String, List<String>> items;
    public Map<String, Integer> barcodeItems;
    public WarehouseTxState state;

    public Double longitude;
    public Double latitude;
    public String internalItem;
}
