package io.agritrack.fish.state;

import java.util.ArrayList;

import io.agritrack.enums.AssetType;

public class RepairRecord {
    public AssetType assetType;
    public int assetTypePos = -1;

    public String assetBC;
    public String maintenanceType;
    public Long nextDateMaintenance;
    public Long estimatedDateWithdrawal;

    public String teamSize;
    public ArrayList<String> repairTeam;
    public String remarks;
    public String site;
    public String supplier;
    public int supplierPos = -1;
    public String manager;
    public String cost;
    public String repairTime;

    public Double longitude;
    public Double latitude;
}
