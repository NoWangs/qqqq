package com.leyou.time.controller;

import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TimeController {
    @GetMapping("/time/getNow")
    public ResponseEntity<String> fetchCurrentTime(){
        String date = new DateTime().toString("yyyy-MM-dd HH:mm:ss");
        return ResponseEntity.ok(date);
    }
}
