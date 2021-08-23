package io.agritrack.fishtrack.data.dto.tx;

import java.util.Date;
import io.agritrack.fishtrack.data.model.tx.ConsumableTransaction;

public class ConsumableTxDTO {

    public Long id;
    public Date timestamp;
    public Double quantity;
    public String barcode;
    public String ftes;
    public String dispatchNote;
    public String transactionType;

    public static ConsumableTransaction convert(ConsumableTxDTO consumableTxDTO) {
        ConsumableTransaction consumableTransaction = new ConsumableTransaction();
        consumableTransaction.id = consumableTxDTO.id;
        consumableTransaction.timestamp = consumableTxDTO.timestamp;
        consumableTransaction.quantity = consumableTxDTO.quantity;
        consumableTransaction.barcode = consumableTxDTO.barcode;
        consumableTransaction.ftes = consumableTxDTO.ftes;
        consumableTransaction.dispatchNote = consumableTxDTO.dispatchNote;
        consumableTransaction.transactionType = consumableTxDTO.transactionType;
        return consumableTransaction;
    }
}
