package io.agritrack.fishtrack.state;

import java.util.List;

import io.agritrack.fishtrack.enums.AssetType;
import io.agritrack.fishtrack.enums.WarehouseTxState;

public class WHTxRecord {
    public AssetType assetType;
    public String from = null;
    public String to = null;

    public int assetTypePos = -1;
    public List<String> items;
    public WarehouseTxState state;
}
