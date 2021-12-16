package io.agritrack.data.dto.tx;

import com.google.android.gms.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.data.model.tx.ConsumableTransaction;
import io.agritrack.data.model.tx.items.IncomingTxWithItems;

public class ConsumableTxDTO {

    public Long id;
    public Integer quantity;
    public String barcode;
    public List<String> incoming_ifco = new LinkedList<String>();
    public String consumableType;
    public String ftes;
    public String dispatch_note;
    public String state;
    public String collection_lot;
    public Double longitude;
    public Double latitude;

    public static ConsumableTxDTO convert(ConsumableTransaction consumableTransaction) {
        ConsumableTxDTO consumableTxDTO = new ConsumableTxDTO();
        consumableTxDTO.id = consumableTransaction.id;
        consumableTxDTO.quantity = consumableTransaction.quantity;
        consumableTxDTO.barcode = consumableTransaction.barcode;
        consumableTxDTO.ftes = consumableTransaction.ftes;
        consumableTxDTO.dispatch_note = consumableTransaction.dispatchNote;
        consumableTxDTO.state = consumableTransaction.state;
        consumableTxDTO.consumableType = consumableTransaction.consumableType;
        consumableTxDTO.collection_lot = consumableTransaction.collectionLot;
        consumableTxDTO.longitude = consumableTransaction.longitude;
        consumableTxDTO.latitude = consumableTransaction.latitude;

        return consumableTxDTO;
    }

    public static ConsumableTxDTO convertIncoming(IncomingTxWithItems consumableTransaction) {
        ConsumableTxDTO consumableTxDTO = new ConsumableTxDTO();
        if (consumableTransaction.incomingTx != null) {
            ConsumableTransaction incomingTx = consumableTransaction.incomingTx;
            consumableTxDTO.id = incomingTx.id;
            consumableTxDTO.quantity = incomingTx.quantity;
            consumableTxDTO.barcode = incomingTx.barcode;
            if (!CollectionUtils.isEmpty(consumableTransaction.ifco)){
                consumableTxDTO.incoming_ifco = consumableTransaction.ifco.stream().map(x-> x.barcode).collect(Collectors.toList());
            }
            consumableTxDTO.ftes = incomingTx.ftes;
            consumableTxDTO.dispatch_note = incomingTx.dispatchNote;
            consumableTxDTO.state = incomingTx.state;
            consumableTxDTO.consumableType = incomingTx.consumableType;
            consumableTxDTO.collection_lot = incomingTx.collectionLot;
            consumableTxDTO.longitude = incomingTx.longitude;
            consumableTxDTO.latitude = incomingTx.latitude;
        }

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
