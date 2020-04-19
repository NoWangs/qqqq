package com.leyou.seckill.controller;

import com.leyou.seckill.service.impl.SeckillPortalServcie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal")
public class SeckillPortalController {

    @Autowired
    private SeckillPortalServcie seckillPortalServcie;

    @GetMapping("/time")
    public ResponseEntity<String> findTime(){
       String date= seckillPortalServcie.getCurrentTime();
       return ResponseEntity.ok(date);
    }
}
