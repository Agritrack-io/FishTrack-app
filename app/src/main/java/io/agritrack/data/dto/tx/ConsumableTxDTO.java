package io.agritrack.data.dto.tx;

import java.util.ArrayList;
import java.util.List;

import io.agritrack.data.model.tx.ConsumableTransaction;

public class ConsumableTxDTO {

    public Long id;
    public Long timestamp;
    public Integer quantity;
    public String barcode;
    public String consumableType;
    public String ftes;
    public String dispatchNote;
    public String state;
    public Double longitude;
    public Double latitude;

    public static ConsumableTxDTO convert(ConsumableTransaction consumableTransaction) {
        ConsumableTxDTO consumableTxDTO = new ConsumableTxDTO();
        consumableTxDTO.id = consumableTransaction.id;
        consumableTxDTO.timestamp = consumableTransaction.timestamp;
        consumableTxDTO.quantity = consumableTransaction.quantity;
        consumableTxDTO.barcode = consumableTransaction.barcode;
        consumableTxDTO.ftes = consumableTransaction.ftes;
        consumableTxDTO.dispatchNote = consumableTransaction.dispatchNote;
        consumableTxDTO.state = consumableTransaction.state;
        consumableTxDTO.consumableType = consumableTransaction.consumableType;
        consumableTxDTO.longitude = consumableTransaction.longitude;
        consumableTxDTO.latitude = consumableTransaction.latitude;

        return consumableTxDTO;
    }

    public static List<ConsumableTxDTO> convert(List<ConsumableTransaction> consumableItems) {
        List<ConsumableTxDTO> result = new ArrayList<>();
        for (ConsumableTransaction consItem : consumableItems) {
            ConsumableTxDTO itemDto = convert(consItem);
            result.add(itemDto);
        }
        return result;
    }
}
