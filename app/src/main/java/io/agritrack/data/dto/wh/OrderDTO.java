package io.agritrack.data.dto.wh;

import io.agritrack.data.model.wh.Order;

public class OrderDTO {

    public Long id;
    public String attr1;
    public String attr2;
    public String orderNo;
    public Short priority;
    public String orderStatus;

    public static Order convert(OrderDTO orderDTO) {
        Order order = new Order();
        order.id = orderDTO.id;
        order.attr1 = orderDTO.attr1;
        order.attr2 = orderDTO.attr2;
        order.orderNo = orderDTO.orderNo;
        order.priority = orderDTO.priority;
        order.orderStatus = orderDTO.orderStatus;
        return order;
    }
}
