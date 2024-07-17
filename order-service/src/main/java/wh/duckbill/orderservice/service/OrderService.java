package wh.duckbill.orderservice.service;

import wh.duckbill.orderservice.dto.OrderDto;
import wh.duckbill.orderservice.jpa.OrderEntity;

public interface OrderService {
    OrderDto createOrder(OrderDto orderDto);

    OrderDto getOrderByOrderId(String orderId);

    Iterable<OrderEntity> getOrdersByUserId(String userId);
}
