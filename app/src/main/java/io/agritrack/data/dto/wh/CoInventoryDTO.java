package io.agritrack.data.dto.wh;

import com.google.android.gms.common.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.data.model.tx.items.CoInventoryTxWithItems;
import io.agritrack.data.model.wh.CoInventory;
import io.agritrack.data.model.wh.RFIDInventory;

public class CoInventoryDTO {

    public Long id;
    public String coInvType;
    public String site;
    public String user;
    public List<String> ifco = new LinkedList<String>();
    public Long performedAt;
    public Double longitude;
    public Double latitude;

    public static CoInventoryDTO convert(CoInventoryTxWithItems inventory) {
        CoInventoryDTO coInventoryDTO = new CoInventoryDTO();
        if (inventory.coInventoryTx != null) {
            CoInventory coInventoryTx = inventory.coInventoryTx;
            coInventoryDTO.id = coInventoryTx.id;
            coInventoryDTO.coInvType = coInventoryTx.coInvType;
            coInventoryDTO.site = coInventoryTx.site;
            coInventoryDTO.user = coInventoryTx.user;
            if (!CollectionUtils.isEmpty(inventory.ifco)){
                coInventoryDTO.ifco = inventory.ifco.stream().map(x-> x.barcode).collect(Collectors.toList());
            }
            coInventoryDTO.performedAt = coInventoryTx.performedAt;
            coInventoryDTO.longitude = coInventoryTx.longitude;
            coInventoryDTO.latitude = coInventoryTx.latitude;
        }

        return coInventoryDTO;
    }
}
