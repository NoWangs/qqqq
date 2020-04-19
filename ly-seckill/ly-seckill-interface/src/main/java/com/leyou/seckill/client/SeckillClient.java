package com.leyou.seckill.client;

import com.leyou.seckill.dto.SeckillPolicyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("seckill-service")
public interface SeckillClient {

    @GetMapping(value = "/list/{date}",name = "根据日期查询秒杀商品")
    public List<SeckillPolicyDTO> findSecKillPolicyList(@PathVariable("date") String date);
}
