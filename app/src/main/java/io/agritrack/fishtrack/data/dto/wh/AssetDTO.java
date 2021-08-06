package io.agritrack.fishtrack.data.dto.wh;

import io.agritrack.fishtrack.data.model.wh.Asset;

public class AssetDTO {

    public Long id;
    public String rfid;
    public String assetType;
    public String amCode;
    public String barcode;
    public String code;
    public String rfidBarcode;
    public String siteId;
    public String cageRFId;
    public String type;
    public String shape;
    public String supplierId;
    public Double depth;
    public Double width;
    public Double length;
    public Double volume;
    public Double perimeter;
    public Double netEyeGirth;
    public Long purchaseDate;
    public Double cost;
    public Boolean enabled;
    public Long withdrewAt;
    public Long estiWithdrewAt;
    public String comments;
    public Integer maxDaysInCage;
    public Integer maxDaysInWater;
    public Integer totalDaysInWater;
    public String storeroomLoc;
    public String erpCode;
    public Long insertInCageAt;
    public String description;

    public static Asset convert(AssetDTO assetDTO) {
        Asset asset = new Asset();
        asset.id = assetDTO.id;
        asset.rfid = assetDTO.rfid;
        asset.assetType = assetDTO.assetType;
        asset.amCode = assetDTO.amCode;
        asset.barcode = assetDTO.barcode;
        asset.code = assetDTO.code;
        asset.id = assetDTO.id;
        asset.code = assetDTO.code;
        asset.rfidBarcode = assetDTO.rfidBarcode;
        asset.siteId = assetDTO.siteId;
        asset.cageRFId = assetDTO.cageRFId;
        asset.type = assetDTO.type;
        asset.shape = assetDTO.shape;
        asset.supplierId = assetDTO.supplierId;
        asset.depth = assetDTO.depth;
        asset.width = assetDTO.width;
        asset.length = assetDTO.length;
        asset.volume = assetDTO.volume;
        asset.perimeter = assetDTO.perimeter;
        asset.netEyeGirth = assetDTO.netEyeGirth;
        asset.purchaseDate = assetDTO.purchaseDate;
        asset.cost = assetDTO.cost;
        asset.enabled = assetDTO.enabled;
        asset.withdrewAt = assetDTO.withdrewAt;
        asset.estiWithdrewAt = assetDTO.estiWithdrewAt;
        asset.comments = assetDTO.comments;
        asset.maxDaysInCage = assetDTO.maxDaysInCage;
        asset.maxDaysInWater = assetDTO.maxDaysInWater;
        asset.totalDaysInWater = assetDTO.totalDaysInWater;
        asset.storeroomLoc = assetDTO.storeroomLoc;
        asset.erpCode = assetDTO.erpCode;
        asset.insertInCageAt = assetDTO.insertInCageAt;
        asset.description = assetDTO.description;
        return asset;
    }
}
