package io.agritrack.data.dto.tx;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import io.agritrack.data.model.tx.PlantTransaction;
import io.agritrack.data.model.tx.ProcessingTransaction;

public class PlantTxDTO {

    public Long id;
    public String site;
    public Long userId;
    public String plant_lot;
    public String asset_rfid;
    public String logger_rfid;
    public String species;

    public static PlantTxDTO convert(PlantTransaction plantTransaction) {
        PlantTxDTO plantTxDTO = new PlantTxDTO();

        plantTxDTO.id = plantTransaction.id;
        plantTxDTO.site = plantTransaction.site;
        plantTxDTO.plant_lot = plantTransaction.plantLot;
        plantTxDTO.asset_rfid = plantTransaction.assetRFID;
        plantTxDTO.logger_rfid = plantTransaction.loggerRFID;
        plantTxDTO.species = plantTransaction.species;

        return plantTxDTO;
    }
}
