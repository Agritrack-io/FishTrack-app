package io.agritrack.data.dto.tx;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

import io.agritrack.data.converter.StringListConverter;
import io.agritrack.data.model.tx.CorrelationTransaction;
import io.agritrack.data.model.tx.StorageTransaction;

public class StorageTxDTO {

    public Long id;
    public Long created_at;
    public List<String> totes_for_storage;
    public List<String> ifco_for_storage;
    public String site;
    public String state;
    public String from;
    public String collection_lot;
    public String total_weight;
    public String to;
    public Double longitude;
    public Double latitude;

    public static StorageTxDTO convert(StorageTransaction storageTx) {
        StorageTxDTO storageTxDTO = new StorageTxDTO();

        storageTxDTO.created_at = storageTx.createdAt;
        storageTxDTO.totes_for_storage = storageTx.totesForStorage;
        storageTxDTO.ifco_for_storage = storageTx.ifcoForStorage;
        storageTxDTO.site = storageTx.site;
        storageTxDTO.state = storageTx.state;
        storageTxDTO.from = storageTx.from;
        storageTxDTO.collection_lot = storageTx.collectionLot;
        storageTxDTO.total_weight = storageTx.totalWeight;
        storageTxDTO.to = storageTx.to;
        storageTxDTO.longitude = storageTx.longitude;
        storageTxDTO.latitude = storageTx.latitude;

        return storageTxDTO;
    }
}
