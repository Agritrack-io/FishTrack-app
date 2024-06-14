package io.agritrack.philosofish.fish.state;

import java.util.List;
import java.util.Map;

import io.agritrack.philosofish.enums.ConsumableType;

public class InventoryWHRecord {
    public long txKey;
    public String selectedSite = null;
    public String assetType;
    public ConsumableType consumableType;
    public String subSite;
    public Map<String, List<String>> items;
    public Map<String, Integer> barcodeItems;
    public String site;
    public Double longitude;
    public Double latitude;

    public int subSitePos = -1;
}
