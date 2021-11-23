package io.agritrack.data.dto.tx;

import java.util.List;

import io.agritrack.data.model.tx.PackageTransaction;

public class PackageTxDTO {

    public Long id;
    public String site;
    public Long user_id;
    public String collection_lot;
    public List<String> totesForProcess;
    public List<String> packagedIfco;
    public List<ShipItemTxDTO> items;
    public Double longitude;
    public Double latitude;

    public static PackageTxDTO convert(PackageTransaction packageTransaction) {
        PackageTxDTO packageTxDTO = new PackageTxDTO();

        packageTxDTO.id = packageTransaction.id;
        packageTxDTO.site = packageTransaction.site;
        packageTxDTO.user_id = packageTransaction.userId;
        packageTxDTO.collection_lot = packageTransaction.collectionLot;
        packageTxDTO.longitude = packageTransaction.longitude;
        packageTxDTO.latitude = packageTransaction.latitude;

        return packageTxDTO;
    }
}
