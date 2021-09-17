package io.agritrack.fishtrack.state;

import java.util.List;

import io.agritrack.fishtrack.enums.AssetType;

public class InventoryWHRecord {
    public String selectedSite = null;
    public AssetType assetType;
    public String inventoryItemType;
    public String site;
    public String subSite;
    public String selectedItemType;

    public int assetTypePos = -1;
    public int subSitePos = -1;
    public List<String> inventoryItems;
}
