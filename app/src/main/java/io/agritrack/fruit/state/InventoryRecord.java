package io.agritrack.fruit.state;

import java.net.ProtocolFamily;
import java.util.List;
import java.util.Map;

import io.agritrack.enums.AssetType;
import io.agritrack.enums.ConsumableType;

public class InventoryRecord {

    public String selectedSite = null;
    public String subSite;
    public List<String> totesItems;
    public List<String> ifcoItems;
    public String site;

    public Double longitude;
    public Double latitude;
    public int subSitePos = -1;
    public AssetType assetType;
    public ConsumableType consumableType;
}
