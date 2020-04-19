package com.leyou.cart.service;


import com.leyou.cart.entity.Cart;
import com.leyou.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    private static String prefix="ly:cart:";
    public void addCartToRedis(String userId, Cart cart) {



        BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps(prefix + userId);
        /*String cartstr = hashOperations.get(skuId.toString());
        if (StringUtils.isNotBlank(cartstr)){
            Cart cart1 = JsonUtils.toBean(cartstr, Cart.class);
            cart.setNum(cart1.getNum()+cart.getNum());

        }
        hashOperations.put(skuId.toString(), JsonUtils.toString(cart));*/
        addCartAll(hashOperations, cart);
    }

    public List<Cart> findCartListFromRedisByUserId(String userId) {
        BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps(prefix + userId);
        List<String> values = hashOperations.values();
        List<Cart> cartList = values.stream().map(str -> {
            return JsonUtils.toBean(str, Cart.class);
        }).collect(Collectors.toList());

        return cartList;
    }

    public void updateCarrtNum(String userId, Long id, Integer num) {
        BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps(prefix + userId);
        String cartstr = hashOperations.get(id.toString());
        Cart cart = JsonUtils.toBean(cartstr, Cart.class);
        cart.setNum(num);
        hashOperations.put(id.toString(), JsonUtils.toString(cart));
    }

    public void deleteCartBySkuId(String userId, Long skuId) {
        BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps(prefix + userId);
        hashOperations.delete(skuId.toString());

    }

    public void addCartFromRedis(String userId, List<Cart> cart) {
        BoundHashOperations<String, String, String> hashOperations = redisTemplate.boundHashOps(prefix + userId);
        for (Cart cart1 : cart) {
            addCartAll(hashOperations, cart1);

        }
    }

    private void addCartAll(BoundHashOperations<String, String, String> hashOperations, Cart cart1) {
        Long skuId = cart1.getSkuId();
        String cartstr = hashOperations.get(skuId.toString());
        if (StringUtils.isNotBlank(cartstr)){
            Cart cart2 = JsonUtils.toBean(cartstr, Cart.class);
            cart1.setNum(cart1.getNum()+cart2.getNum());
        }
        hashOperations.put(skuId.toString(), JsonUtils.toString(cart1));
    }
}
