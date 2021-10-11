package io.agritrack.data.dto.wh;

import io.agritrack.data.model.wh.OrderItem;

public class OrderItemDTO {

    public Long id;
    public String code;
    public String containerLvl1;
    public String containerLvl2;
    public Double quantity;

    public static OrderItem convert(OrderItemDTO orderItemDTO) {
        OrderItem orderItem = new OrderItem();
        orderItem.id = orderItemDTO.id;
        orderItem.code = orderItemDTO.code;
        orderItem.containerLvl1 = orderItemDTO.containerLvl1;
        orderItem.containerLvl2 = orderItemDTO.containerLvl2;
        orderItem.quantity = orderItemDTO.quantity;
        return orderItem;
    }
}
