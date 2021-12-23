package io.agritrack.data.dto.wh;

import com.google.android.gms.common.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.data.model.tx.items.IfcoInventoryTxWithItems;
import io.agritrack.data.model.wh.CoInventory;

public class IfcoInventoryDTO {

    public String inventory_type;
    public String site;
    public String user;
    public List<String> ifco = new LinkedList<String>();
    public Long created_at;
    public Double longitude;
    public Double latitude;

    public static IfcoInventoryDTO convert(IfcoInventoryTxWithItems inventory) {
        IfcoInventoryDTO ifcoInventoryDTO = new IfcoInventoryDTO();
        if (inventory.coInventoryTx != null) {
            CoInventory coInventoryTx = inventory.coInventoryTx;
            ifcoInventoryDTO.inventory_type = coInventoryTx.coInvType;
            ifcoInventoryDTO.site = coInventoryTx.site;
            ifcoInventoryDTO.user = coInventoryTx.user;
            if (!CollectionUtils.isEmpty(inventory.ifco)){
                ifcoInventoryDTO.ifco = inventory.ifco.stream().map(x-> x.barcode).collect(Collectors.toList());
            }
            ifcoInventoryDTO.created_at = coInventoryTx.performedAt;
            ifcoInventoryDTO.longitude = coInventoryTx.longitude;
            ifcoInventoryDTO.latitude = coInventoryTx.latitude;
        }

        return ifcoInventoryDTO;
    }
}
