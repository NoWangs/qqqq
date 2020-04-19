package com.leyou.seckill.service.impl;

import com.leyou.client.TimeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeckillPortalServcie {

    @Autowired
    private TimeClient timeClient;
    public String getCurrentTime() {

        return timeClient.fetchCurrentTime();
    }
}
