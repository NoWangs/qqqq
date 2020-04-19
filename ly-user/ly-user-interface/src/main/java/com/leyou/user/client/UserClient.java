package com.leyou.user.client;

import com.leyou.user.dto.UserAddressDTO;
import com.leyou.user.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("user-service")
public interface UserClient {

    @GetMapping(value = "/query",name = "根据用户名和密码查询用户")
    public UserDTO queryUsernameAndPassword(@RequestParam("username")String username,
                                                            @RequestParam("password")String password);


    @GetMapping("/address/byId")
    public UserAddressDTO findUserAddressById(@RequestParam("id")Long id);
}
