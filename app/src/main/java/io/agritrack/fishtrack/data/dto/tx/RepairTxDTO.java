package io.agritrack.fishtrack.data.dto.tx;

import java.util.Date;
import io.agritrack.fishtrack.data.model.tx.RepairTransaction;

public class RepairTxDTO {

    public Long id;
    public String assetRFID;
    public String assetType;
    public String repairType;
    public Boolean onField;
    public Boolean internalRepair;
    public String repairFTEs;
    public String repairManager;
    public String description;
    public Double cost;
    public Date timestamp;
    public Date nextRepair;
    public Date estimatedWithdrawal;

    public static RepairTransaction convert(RepairTxDTO repairTxDTO) {
        RepairTransaction repairTransaction = new RepairTransaction();
        repairTransaction.id = repairTxDTO.id;
        repairTransaction.assetRFID = repairTxDTO.assetRFID;
        repairTransaction.assetType = repairTxDTO.assetType;
        repairTransaction.repairType = repairTxDTO.repairType;
        repairTransaction.onField = repairTxDTO.onField;
        repairTransaction.internalRepair = repairTxDTO.internalRepair;
        repairTransaction.repairFTEs = repairTxDTO.repairFTEs;
        repairTransaction.repairManager = repairTxDTO.repairManager;
        repairTransaction.description = repairTxDTO.description;
        repairTransaction.cost = repairTxDTO.cost;
        repairTransaction.timestamp = repairTxDTO.timestamp;
        repairTransaction.nextRepair = repairTxDTO.nextRepair;
        repairTransaction.estimatedWithdrawal = repairTxDTO.estimatedWithdrawal;
        return repairTransaction;
    }
}
