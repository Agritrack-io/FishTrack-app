package io.agritrack.fishtrack.state;

import java.util.List;
import java.util.Map;

import io.agritrack.fishtrack.enums.AssetType;
import io.agritrack.fishtrack.enums.ConsumableType;
import io.agritrack.fishtrack.enums.WarehouseTxState;

public class InventoryWHRecord {
    public String selectedSite = null;
    public AssetType assetType;
    public ConsumableType consumableType;
    public String subSite;
    public Map<String, List<String>> items;
    public Map<String, Integer> barcodeItems;
    public String site;
    public Double longitude;
    public Double latitude;

    public int subSitePos = -1;
}
