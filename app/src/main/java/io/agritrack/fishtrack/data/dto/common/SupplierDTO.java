package io.agritrack.fishtrack.data.dto.common;

import io.agritrack.fishtrack.data.model.common.Supplier;

public class SupplierDTO {

    public Long id;
    public String code;
    public String name;
    public String mainAddress;
    public String shippingAddress;
    public String vat;
    public String currency;
    public Boolean enabled;

    public static Supplier convert(SupplierDTO supplierDTO) {
        Supplier supplier = new Supplier();
        supplier.id = supplierDTO.id;
        supplier.code = supplierDTO.code;
        supplier.name = supplierDTO.name;
        supplier.mainAddress = supplierDTO.mainAddress;
        supplier.shippingAddress = supplierDTO.shippingAddress;
        supplier.vat = supplierDTO.vat;
        supplier.currency = supplierDTO.currency;
        supplier.enabled = supplierDTO.enabled;
        return supplier;
    }
}
