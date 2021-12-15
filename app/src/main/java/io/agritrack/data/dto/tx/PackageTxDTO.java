package io.agritrack.data.dto.tx;

import com.google.android.gms.common.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.data.model.tx.CollectTransaction;
import io.agritrack.data.model.tx.PackageTransaction;
import io.agritrack.data.model.tx.items.PackageTxWithItems;

public class PackageTxDTO {

    public Long id;
    public String user;
    public String site;
    public String collection_lot;
    public List<String> totes_for_process = new LinkedList<String>();
    public Integer totes_cnt;
    public List<String> packaged_ifco = new LinkedList<String>();
    public Integer ifco_cnt;
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
            packageTxDTO.collection_lot = packageTx.collectionLot;
            if (!CollectionUtils.isEmpty(packageTransaction.totes)){
                packageTxDTO.totes_for_process = packageTransaction.totes.stream().map(x-> x.epc).collect(Collectors.toList());
            }
            packageTxDTO.totes_cnt = packageTx.totesCnt;
            if (!CollectionUtils.isEmpty(packageTransaction.ifco)){
                packageTxDTO.packaged_ifco = packageTransaction.ifco.stream().map(x-> x.barcode).collect(Collectors.toList());
            }
            packageTxDTO.ifco_cnt = packageTx.ifcoCnt;
            packageTxDTO.longitude = packageTx.longitude;
            packageTxDTO.latitude = packageTx.latitude;
        }

        return packageTxDTO;
    }
}
