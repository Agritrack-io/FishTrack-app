package io.agritrack.fishtrack.state;

import java.util.List;
import java.util.Map;

import io.agritrack.fishtrack.enums.AssetType;
import io.agritrack.fishtrack.enums.WarehouseTxState;

public class InventoryWHRecord {
    public String selectedSite = null;
    public AssetType assetType;
    public String subSite;
    public Map<String, List<String>> items;

    public int subSitePos = -1;
}
