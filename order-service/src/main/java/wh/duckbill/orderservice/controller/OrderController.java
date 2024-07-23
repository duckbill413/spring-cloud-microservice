package wh.duckbill.orderservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wh.duckbill.orderservice.dto.OrderDto;
import wh.duckbill.orderservice.jpa.OrderEntity;
import wh.duckbill.orderservice.messagequeue.KafkaProducer;
import wh.duckbill.orderservice.messagequeue.OrderProducer;
import wh.duckbill.orderservice.service.OrderService;
import wh.duckbill.orderservice.vo.RequestOrder;
import wh.duckbill.orderservice.vo.ResponseOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/order-service")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final ModelMapper modelMapper;
    private final Environment env;
    private final KafkaProducer kafkaProducer;
    private final OrderProducer orderProducer;

    @GetMapping("/health-check")
    public String status() {
        return String.format("It's working in User Service on PORT %s", env.getProperty("local.server.port"));
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<ResponseOrder> createOrder(
            @PathVariable String userId,
            @RequestBody RequestOrder requestOrder
    ) {
        OrderDto orderDto = modelMapper.map(requestOrder, OrderDto.class);
        orderDto.setUserId(userId);

        /* jpa */
//        OrderDto createdOrder = orderService.createOrder(orderDto);
//        ResponseOrder responseOrder = modelMapper.map(createdOrder, ResponseOrder.class);

        /* kafka */
        orderDto.setOrderId(UUID.randomUUID().toString());
        orderDto.setTotalPrice(requestOrder.getUnitPrice() * requestOrder.getQty());

        /* send this order to the kafka */
        // catalog service 에 order 전송
        kafkaProducer.send("example-catalog-topic", orderDto); // config server 에 topic name 지정 권장
        // orders db 에 insert 요청
        orderProducer.send("orders", orderDto); // config server 에 naming 지정 권장
        ResponseOrder responseOrder = modelMapper.map(orderDto, ResponseOrder.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ResponseOrder> getOrderByOrderId(@PathVariable("orderId") String orderId) {
        OrderDto order = orderService.getOrderByOrderId(orderId);
        ResponseOrder responseOrder = modelMapper.map(order, ResponseOrder.class);
        return ResponseEntity.status(HttpStatus.OK).body(responseOrder);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<ResponseOrder>> getOrdersByUserId(@PathVariable("userId") String userId) {
        Iterable<OrderEntity> orders = orderService.getOrdersByUserId(userId);
        List<ResponseOrder> result = new ArrayList<>();
        orders.forEach(v -> result.add(modelMapper.map(v, ResponseOrder.class)));
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
