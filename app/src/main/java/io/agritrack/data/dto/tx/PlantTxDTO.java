package io.agritrack.data.dto.tx;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import io.agritrack.data.model.tx.PlantTransaction;
import io.agritrack.data.model.tx.ProcessingTransaction;

public class PlantTxDTO {

    public Long id;
    public String site;
    public String user;
    public String plant_lot;
    public String asset_rfid;
    public String species;
    public Double longitude;
    public Double latitude;
    public Long created_at;

    public static PlantTxDTO convert(PlantTransaction plantTransaction) {
        PlantTxDTO plantTxDTO = new PlantTxDTO();

        plantTxDTO.id = plantTransaction.id;
        plantTxDTO.site = plantTransaction.site;
        plantTxDTO.user = plantTransaction.userId;
        plantTxDTO.plant_lot = plantTransaction.plantLot;
        plantTxDTO.asset_rfid = plantTransaction.assetRFID;
        plantTxDTO.species = plantTransaction.species;
        plantTxDTO.longitude = plantTransaction.longitude;
        plantTxDTO.latitude = plantTransaction.latitude;
        plantTxDTO.created_at = plantTransaction.createdAt;

        return plantTxDTO;
    }
}
