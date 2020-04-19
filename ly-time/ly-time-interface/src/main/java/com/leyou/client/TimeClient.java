package com.leyou.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
@FeignClient("time-service")
public interface TimeClient {

    @GetMapping("/time/getNow")
    public String fetchCurrentTime();
}
