package io.agritrack.data.dto.tx;

import java.util.Date;
import io.agritrack.data.model.tx.RepairTransaction;

public class RepairTxDTO {

    public Long id;
    public String assetRFID;
    public String assetType;
    public String repairType;
    public String repairTime;
    public Boolean onField;
    public Boolean internalRepair;
    public String repairFTEs;
    public String repairManager;
    public String description;
    public Double cost;
    public Long timestamp;
    public Date nextRepair;
    public Date estimatedWithdrawal;
    public Double longitude;
    public Double latitude;

    public static RepairTransaction convert(RepairTxDTO repairTxDTO) {
        RepairTransaction repairTransaction = new RepairTransaction();
        repairTransaction.id = repairTxDTO.id;
        repairTransaction.assetRFID = repairTxDTO.assetRFID;
        repairTransaction.assetType = repairTxDTO.assetType;
        repairTransaction.repairType = repairTxDTO.repairType;
        repairTransaction.repairTime = repairTxDTO.repairTime;
        repairTransaction.onField = repairTxDTO.onField;
        repairTransaction.internalRepair = repairTxDTO.internalRepair;
        repairTransaction.repairFTEs = repairTxDTO.repairFTEs;
        repairTransaction.repairManager = repairTxDTO.repairManager;
        repairTransaction.description = repairTxDTO.description;
        repairTransaction.cost = repairTxDTO.cost;
        repairTransaction.timestamp = repairTxDTO.timestamp;
        repairTransaction.nextRepair = repairTxDTO.nextRepair;
        repairTransaction.estimatedWithdrawal = repairTxDTO.estimatedWithdrawal;
        repairTransaction.longitude = repairTxDTO.longitude;
        repairTransaction.latitude = repairTxDTO.latitude;

        return repairTransaction;
    }
}
