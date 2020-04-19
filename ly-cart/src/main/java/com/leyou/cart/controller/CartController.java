package com.leyou.cart.controller;

import com.leyou.auth.entity.UserHolder;
import com.leyou.cart.entity.Cart;
import com.leyou.cart.service.CartService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping(name="向redis中添加购物车数据")
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        String userId = UserHolder.getUserId();
        cartService.addCartToRedis(userId,cart);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    @GetMapping(value = "/list" ,name = "从redis中获取当前登录人的购物车数据集合")
    public ResponseEntity<List<Cart>> findCartListFromRedis(){
        String userId = UserHolder.getUserId();
        List<Cart> cartList = cartService.findCartListFromRedisByUserId(userId);
        return ResponseEntity.ok(cartList);
    }
    @PutMapping(name = "修改购物车数量")
    public ResponseEntity<Void> updateCarrtNum(@RequestParam("id")Long id,
                                               @RequestParam("num")Integer num){
        String userId = UserHolder.getUserId();
        cartService.updateCarrtNum(userId,id,num);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping(value = "/{skuId}",name = "删除购物车")
    public ResponseEntity<Void> deleteCartBySkuId(@PathVariable("skuId")Long skuId){
        String userId = UserHolder.getUserId();
        cartService.deleteCartBySkuId(userId,skuId);
        return ResponseEntity.ok().build();
    }
    @PostMapping(value = "/list",name = "合并购物车")
    public ResponseEntity<Void> addCartFromRedis(@RequestBody List<Cart> cart){
        String userId = UserHolder.getUserId();
        cartService.addCartFromRedis(userId,cart);
        return ResponseEntity.ok().build();
    }


}
