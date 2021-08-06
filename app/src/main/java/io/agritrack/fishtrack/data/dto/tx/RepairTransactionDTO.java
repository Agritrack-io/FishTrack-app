package io.agritrack.fishtrack.data.dto.tx;

import java.util.Date;
import io.agritrack.fishtrack.data.model.tx.RepairTransaction;

public class RepairTransactionDTO {

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

    public static RepairTransaction convert(RepairTransactionDTO repairTransactionDTO) {
        RepairTransaction repairTransaction = new RepairTransaction();
        repairTransaction.id = repairTransactionDTO.id;
        repairTransaction.assetRFID = repairTransactionDTO.assetRFID;
        repairTransaction.assetType = repairTransactionDTO.assetType;
        repairTransaction.repairType = repairTransactionDTO.repairType;
        repairTransaction.onField = repairTransactionDTO.onField;
        repairTransaction.internalRepair = repairTransactionDTO.internalRepair;
        repairTransaction.repairFTEs = repairTransactionDTO.repairFTEs;
        repairTransaction.repairManager = repairTransactionDTO.repairManager;
        repairTransaction.description = repairTransactionDTO.description;
        repairTransaction.cost = repairTransactionDTO.cost;
        repairTransaction.timestamp = repairTransactionDTO.timestamp;
        repairTransaction.nextRepair = repairTransactionDTO.nextRepair;
        repairTransaction.estimatedWithdrawal = repairTransactionDTO.estimatedWithdrawal;
        return repairTransaction;
    }
}
