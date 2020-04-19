package com.leyou.controller;


import com.leyou.auth.entity.UserHolder;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.vo.OrderVO;
import com.leyou.service.impl.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class orderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/order")
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO orderDTO){
        String userId = UserHolder.getUserId();
        Long orderId=orderService.createOrder(orderDTO,userId);
        return ResponseEntity.ok(orderId);
    }
    @GetMapping(value = "/order/{id}" ,name = "查询订单")
    public ResponseEntity<OrderVO> findOrderById(@PathVariable("id")Long orderId){
        OrderVO orderVO= orderService.findOrderById(orderId);
        return ResponseEntity.ok(orderVO);
    }
    @GetMapping(value = "/order/url/{id}" ,name = "获取微信的一个支付链接")
    public ResponseEntity<String> getCodeUrl(@PathVariable("id") Long orderId){
        String url = orderService.getCodeUrl(orderId);
        return ResponseEntity.ok(url);
    }

    @GetMapping(value = "/order/state/{id}" ,name = "查询订单的支付状态")
    public ResponseEntity<Integer> queryOrderPayState(@PathVariable("id") Long orderId){
        Integer state = orderService.queryOrderPayState(orderId);
        return ResponseEntity.ok(state);
    }


}
