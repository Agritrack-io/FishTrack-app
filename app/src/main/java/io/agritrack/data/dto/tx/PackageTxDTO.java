package io.agritrack.data.dto.tx;

import com.google.android.gms.common.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.data.model.tx.PackageTransaction;
import io.agritrack.data.model.tx.items.PackageTxWithItems;

public class PackageTxDTO {

    public Long id;
    public String user;
    public String site;
    public String packaging_lot;
    public String collection_lot;
    public String packaging_site;
    public String storage_site;
    public List<String> totes_for_process = new LinkedList<String>();
    public Integer totes_cnt;
    public List<String> palette_barcode = new LinkedList<String>();
    public Integer palette_cnt;
    public List<TotesTxDTO> items;
    public Double longitude;
    public Double latitude;

    public static PackageTxDTO convert(PackageTxWithItems packageTransaction) {
        PackageTxDTO packageTxDTO = new PackageTxDTO();
        if (packageTransaction.packageTx != null) {
            PackageTransaction packageTx = packageTransaction.packageTx;
            packageTxDTO.id = packageTx.id;
            packageTxDTO.site = packageTx.site;
            packageTxDTO.user = packageTx.user;
            packageTxDTO.packaging_lot = packageTx.packagingLot;
            packageTxDTO.collection_lot = packageTx.collectionLot;
            packageTxDTO.packaging_site = packageTx.packagingSite;
            packageTxDTO.storage_site = packageTx.storageSite;
            if (!CollectionUtils.isEmpty(packageTransaction.totes)){
                packageTxDTO.totes_for_process = packageTransaction.totes.stream().map(x-> x.epc).collect(Collectors.toList());
            }
            packageTxDTO.totes_cnt = packageTx.totesCnt;
            if (!CollectionUtils.isEmpty(packageTransaction.ifco)){
                packageTxDTO.palette_barcode = packageTransaction.ifco.stream().map(x-> x.barcode).collect(Collectors.toList());
            }
            packageTxDTO.palette_cnt = packageTx.paletteCnt;
            packageTxDTO.longitude = packageTx.longitude;
            packageTxDTO.latitude = packageTx.latitude;
        }

        return packageTxDTO;
    }
}
