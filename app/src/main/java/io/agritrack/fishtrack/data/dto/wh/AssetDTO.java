package io.agritrack.fishtrack.data.dto.wh;

import io.agritrack.fishtrack.data.model.wh.Asset;

public class AssetDTO {

    public Long id;
    public String rfid;
    public String asset_type;
    public String amCode;
    public String barcode;
    public String code;
    public String rfid_barcode;
    public String site;
    public String cage_RFID;
    public String type;
    public String shape;
    public String supplier;
    public Double depth;
    public Double width;
    public Double length;
    public Double volume;
    public Double perimeter;
    public Double net_eye_girth;
    public Long purchase_date;
    public Double cost;
    public Boolean enabled;
    public Long withdrew_at;
    public Long est_withdrew_at;
    public String comments;
    public Integer max_days_in_cage;
    public Integer max_days_in_water;
    public Integer total_days_in_water;
    public String storeroom_loc;
    public String erp_code;
    public Long insert_in_cage_at;
    public String description;

    public static Asset convert(AssetDTO assetDTO) {
        Asset asset = new Asset();
        asset.id = assetDTO.id;
        asset.rfid = assetDTO.rfid;
        asset.assetType = assetDTO.asset_type;
        asset.amCode = assetDTO.amCode;
        asset.barcode = assetDTO.barcode;
        asset.code = assetDTO.code;
        asset.rfidBarcode = assetDTO.rfid_barcode;
        asset.siteId = assetDTO.site;
        asset.cageRFId = assetDTO.cage_RFID;
        asset.type = assetDTO.type;
        asset.shape = assetDTO.shape;
        asset.supplierId = assetDTO.supplier;
        asset.depth = assetDTO.depth;
        asset.width = assetDTO.width;
        asset.length = assetDTO.length;
        asset.volume = assetDTO.volume;
        asset.perimeter = assetDTO.perimeter;
        asset.netEyeGirth = assetDTO.net_eye_girth;
        asset.purchaseDate = assetDTO.purchase_date;
        asset.cost = assetDTO.cost;
        asset.enabled = assetDTO.enabled;
        asset.withdrewAt = assetDTO.withdrew_at;
        asset.estiWithdrewAt = assetDTO.est_withdrew_at;
        asset.comments = assetDTO.comments;
        asset.maxDaysInCage = assetDTO.max_days_in_cage;
        asset.maxDaysInWater = assetDTO.max_days_in_water;
        asset.totalDaysInWater = assetDTO.total_days_in_water;
        asset.storeroomLoc = assetDTO.storeroom_loc;
        asset.erpCode = assetDTO.erp_code;
        asset.insertInCageAt = assetDTO.insert_in_cage_at;
        asset.description = assetDTO.description;
        return asset;
    }
}
