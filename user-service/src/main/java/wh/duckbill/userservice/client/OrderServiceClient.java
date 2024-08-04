package wh.duckbill.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import wh.duckbill.userservice.error.FeignErrorDecoder;
import wh.duckbill.userservice.vo.ResponseOrder;

import java.util.List;

@FeignClient(name = "order-service", url = "${order-service-url}", configuration = FeignErrorDecoder.class)
public interface OrderServiceClient {
    @GetMapping("/order-service/{userId}/orders")
    List<ResponseOrder> getOrders(@PathVariable String userId);
}
