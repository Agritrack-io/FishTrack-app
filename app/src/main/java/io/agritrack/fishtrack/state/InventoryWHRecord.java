package io.agritrack.fishtrack.state;

import java.util.List;

import io.agritrack.fishtrack.enums.AssetType;

public class InventoryWHRecord {
    public String selectedSite = null;
    public AssetType assetType;

    public int assetTypePos = -1;
    public int selectedSitePos = -1;
    public List<String> inventoryItems;
}
