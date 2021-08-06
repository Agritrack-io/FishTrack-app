package io.agritrack.fishtrack.data.dto.tx;

import java.util.Date;
import io.agritrack.fishtrack.data.model.tx.ConsumableTransaction;

public class ConsumableTransactionDTO {

    public Long id;
    public Date timestamp;
    public Double quantity;
    public String barcode;
    public String ftes;
    public String dispatchNote;
    public String transactionType;

    public static ConsumableTransaction convert(ConsumableTransactionDTO consumableTransactionDTO) {
        ConsumableTransaction consumableTransaction = new ConsumableTransaction();
        consumableTransaction.id = consumableTransactionDTO.id;
        consumableTransaction.timestamp = consumableTransactionDTO.timestamp;
        consumableTransaction.quantity = consumableTransactionDTO.quantity;
        consumableTransaction.barcode = consumableTransactionDTO.barcode;
        consumableTransaction.ftes = consumableTransactionDTO.ftes;
        consumableTransaction.dispatchNote = consumableTransactionDTO.dispatchNote;
        consumableTransaction.transactionType = consumableTransactionDTO.transactionType;
        return consumableTransaction;
    }
}
