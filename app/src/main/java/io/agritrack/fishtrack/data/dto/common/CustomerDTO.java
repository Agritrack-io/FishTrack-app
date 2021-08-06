package io.agritrack.fishtrack.data.dto.common;

import io.agritrack.fishtrack.data.model.common.Customer;

public class CustomerDTO {

    public Long id;
    public String code;
    public String name;
    public String mainAddress;
    public String shippingAddress;
    public String vat;
    public String currency;
    public Boolean enabled;

    public static Customer convert(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        customer.id = customerDTO.id;
        customer.code = customerDTO.code;
        customer.name = customerDTO.name;
        customer.mainAddress = customerDTO.mainAddress;
        customer.shippingAddress = customerDTO.shippingAddress;
        customer.vat = customerDTO.vat;
        customer.currency = customerDTO.currency;
        customer.enabled = customerDTO.enabled;
        return customer;
    }
}
