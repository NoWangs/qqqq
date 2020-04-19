package com.leyou.auth.controller;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.service.AuthService;
import com.netflix.client.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired(required = false)
    private HttpServletResponse response;
    @Autowired
    private HttpServletRequest request;
    @PostMapping(value = "/login",name = "登录")
    public ResponseEntity<Void> login(@RequestParam("username")String username,
                                      @RequestParam("password")String password)throws Exception{

    authService.login(username,password,response);
    return ResponseEntity.ok().build();
    }
    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verify(){
        UserInfo userInfo=authService.verify(request,response);
        return ResponseEntity.ok(userInfo);
    }
    @PostMapping(value = "/logout",name = "退出")
    public ResponseEntity<Void> logout(){
        authService.logout(request,response);
        return ResponseEntity.ok().build();
    }
}
