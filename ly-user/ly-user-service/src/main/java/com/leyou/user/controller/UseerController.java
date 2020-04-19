package com.leyou.user.controller;

import com.leyou.exception.LyException;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.TbUser;
import com.leyou.user.service.TbUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UseerController {
    @Autowired
    private TbUserService userService;
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkUserData(@PathVariable("data") String data,
                                                 @PathVariable("type") Integer type){

       Boolean b= userService.checkUserData(data,type);
       // System.out.println(b);
       return ResponseEntity.ok(b);
    }
    @PostMapping(value = "/code",name = "发送验证码")
    public ResponseEntity<Void> sendCode(@RequestParam("phone")String phone){
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    @PostMapping(value = "/register",name = "注册")
    public ResponseEntity<Void> register(@Valid TbUser tbUser, BindingResult result, @RequestParam("code") String code){
        if (result.hasErrors()){
            List<FieldError> re = result.getFieldErrors();
            String errorMessage = re.stream().map(FieldError::getDefaultMessage).collect(Collectors.joining("|"));
            throw new LyException(500, errorMessage);
        }
        userService.register(tbUser,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @GetMapping(value = "/query",name = "根据用户名和密码查询用户")
    public ResponseEntity<UserDTO> queryUsernameAndPassword(@RequestParam("username")String username,
                                                            @RequestParam("password")String password){
        UserDTO userDTO= userService.queryUsernameAndPassword(username,password);
        return ResponseEntity.ok(userDTO);
    }
}
