package io.agritrack.data.dto.tx;

import com.google.android.gms.common.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.data.model.tx.PackageTransaction;
import io.agritrack.data.model.tx.StorageTransaction;
import io.agritrack.data.model.tx.items.StorageTxWithItems;

public class StorageTxDTO {

    public Long id;
    public String user;
    public Long created_at;
    public List<String> totes_for_storage = new LinkedList<String>();
    public Integer totes_cnt;
    public List<String> palette_barcode = new LinkedList<String>();
    public Integer palette_cnt;
    public String site;
    public String category;
    public String source_site;
    public String collection_lot;
    public String packaging_lot;
    public String total_weight;
    public String target_site;
    public Double longitude;
    public Double latitude;

    public static StorageTxDTO convert(StorageTxWithItems storageTransaction) {
        StorageTxDTO storageTxDTO = new StorageTxDTO();
        if (storageTransaction.storageTx != null) {
            StorageTransaction storageTx = storageTransaction.storageTx;
            storageTxDTO.user = storageTx.user;
            storageTxDTO.created_at = storageTx.createdAt;
            if (!CollectionUtils.isEmpty(storageTransaction.totes)){
                storageTxDTO.totes_for_storage = storageTransaction.totes.stream().map(x-> x.epc).collect(Collectors.toList());
            }
            storageTxDTO.totes_cnt = storageTx.totesCnt;
            if (!CollectionUtils.isEmpty(storageTransaction.ifco)){
                storageTxDTO.palette_barcode = storageTransaction.ifco.stream().map(x-> x.barcode).collect(Collectors.toList());
            }
            storageTxDTO.palette_cnt = storageTx.ifcoCnt;
            storageTxDTO.site = storageTx.site;
            storageTxDTO.category = storageTx.category;
            storageTxDTO.source_site = storageTx.from;
            storageTxDTO.collection_lot = storageTx.collectionLot;
            storageTxDTO.packaging_lot = storageTx.packagingLot;
            storageTxDTO.total_weight = storageTx.totalWeight;
            storageTxDTO.target_site = storageTx.to;
            storageTxDTO.longitude = storageTx.longitude;
            storageTxDTO.latitude = storageTx.latitude;
        }

        return storageTxDTO;
    }
}
