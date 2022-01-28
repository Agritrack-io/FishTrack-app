package io.agritrack.data.dto.tx;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

import com.google.android.gms.common.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import io.agritrack.data.model.tx.ShippingTransaction;
import io.agritrack.data.model.tx.StorageTransaction;
import io.agritrack.data.model.tx.TransportTransaction;
import io.agritrack.data.model.tx.items.ShippingTxWithItems;

public class ShippingTxDTO {

    public Long id;
    public String truck_license_plate;
    public String driver_name;
    public String driver_phone;
    public String user;
    public String site;
    public List<String> palette_barcode = new LinkedList<String>();
    public Integer palette_cnt;
    public String customer;
    public String packaging_lot;
    public Double longitude;
    public Double latitude;

    public static ShippingTxDTO convert(ShippingTxWithItems shipping) {
        ShippingTxDTO shippingTxDto = new ShippingTxDTO();
        if (shipping.shippingTx != null) {
            ShippingTransaction shippingTx = shipping.shippingTx;
            shippingTxDto.id = shippingTx.id;
            shippingTxDto.truck_license_plate = shippingTx.truckLicensePlate;
            shippingTxDto.driver_name = shippingTx.driverName;
            shippingTxDto.driver_phone = shippingTx.driverPhone;
            shippingTxDto.user = shippingTx.user;
            shippingTxDto.site = shippingTx.site;
            shippingTxDto.palette_cnt = shippingTx.ifcoCnt;
            if (!CollectionUtils.isEmpty(shipping.ifco)){
                shippingTxDto.palette_barcode = shipping.ifco.stream().map(x-> x.barcode).collect(Collectors.toList());
            }
            shippingTxDto.customer = shippingTx.customer;
            shippingTxDto.packaging_lot = shippingTx.packagingLot;
            shippingTxDto.longitude = shippingTx.longitude;
            shippingTxDto.latitude = shippingTx.latitude;
        }

        return shippingTxDto;
    }
}
