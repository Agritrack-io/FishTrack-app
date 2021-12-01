package io.agritrack.data.dto.tx;

import io.agritrack.data.model.tx.CollectTransaction;

public class CollectTxDTO {

    public Long id;
    public String site;
    public String user_id;
    public String plant_lot;
    public String asset_rfid;
    public String species;
    private String collection_lot;
    private Integer totes_cnt;
    public Double longitude;
    public Double latitude;
    public Long created_at;

    public static CollectTxDTO convert(CollectTransaction collectTransaction) {
        CollectTxDTO collectTxDTO = new CollectTxDTO();

        collectTxDTO.id = collectTransaction.id;
        collectTxDTO.site = collectTransaction.site;
        collectTxDTO.user_id = collectTransaction.userId;
        collectTxDTO.plant_lot = collectTransaction.plantLot;
        collectTxDTO.asset_rfid = collectTransaction.assetRFID;
        collectTxDTO.species = collectTransaction.species;
        collectTxDTO.collection_lot = collectTransaction.collectionLot;
        collectTxDTO.totes_cnt = collectTransaction.totesCnt;
        collectTxDTO.longitude = collectTransaction.longitude;
        collectTxDTO.latitude = collectTransaction.latitude;
        collectTxDTO.created_at = collectTransaction.createdAt;

        return collectTxDTO;
    }
}
