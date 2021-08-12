package io.agritrack.fishtrack.data.dto.common;

import io.agritrack.fishtrack.data.model.common.Customer;

public class CustomerDTO {

    public Long id;
    public String customer_code;
    public String customer_name;
    public String main_address;
    public String shipping_address;
    public String vat;
    public String currency;
    public Boolean enabled;

    public static Customer convert(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        customer.id = customerDTO.id;
        customer.code = customerDTO.customer_code;
        customer.name = customerDTO.customer_name;
        customer.mainAddress = customerDTO.main_address;
        customer.shippingAddress = customerDTO.shipping_address;
        customer.vat = customerDTO.vat;
        customer.currency = customerDTO.currency;
        customer.enabled = customerDTO.enabled;
        return customer;
    }
}
