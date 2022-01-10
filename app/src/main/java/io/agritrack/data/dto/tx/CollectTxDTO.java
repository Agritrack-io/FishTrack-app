package io.agritrack.data.dto.tx;

import com.google.android.gms.common.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.data.model.tx.CollectTransaction;
import io.agritrack.data.model.tx.items.CollectionTxWithItems;

public class CollectTxDTO {

    public Long id;
    public String site;
    public String user;
    public List<String> totes = new LinkedList<String>();
    public String asset_rfid;
    public String species;
    public String collection_lot;
    public Integer totes_cnt;
    public Double longitude;
    public Double latitude;
    public Long created_at;

    public static CollectTxDTO convert(CollectionTxWithItems collectTransaction) {
        CollectTxDTO collectTxDTO = new CollectTxDTO();
        if (collectTransaction.collectTx != null){
            CollectTransaction collectTx = collectTransaction.collectTx;
            collectTxDTO.id = collectTx.id;
            collectTxDTO.site = collectTx.site;
            collectTxDTO.user = collectTx.userId;
            if (!CollectionUtils.isEmpty(collectTransaction.items)){
                collectTxDTO.totes = collectTransaction.items.stream().map(x-> x.epc).collect(Collectors.toList());
            }
            collectTxDTO.asset_rfid = collectTx.assetRFID;
            collectTxDTO.species = collectTx.species;
            collectTxDTO.collection_lot = collectTx.collectionLot;
            collectTxDTO.totes_cnt = collectTx.totesCnt;
            collectTxDTO.longitude = collectTx.longitude;
            collectTxDTO.latitude = collectTx.latitude;
            collectTxDTO.created_at = collectTx.createdAt;
        }

        return collectTxDTO;
    }
}
