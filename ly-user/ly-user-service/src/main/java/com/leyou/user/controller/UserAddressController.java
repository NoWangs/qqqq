package com.leyou.user.controller;

import com.leyou.auth.entity.UserHolder;
import com.leyou.user.dto.UserAddressDTO;
import com.leyou.user.entity.TbUserAddress;
import com.leyou.user.service.TbUserAddressService;
import com.leyou.utils.BeanHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserAddressController {
    @Autowired
    private TbUserAddressService userAddressService;
    @GetMapping("/address/byId")
    public ResponseEntity<UserAddressDTO> findUserAddressById(@RequestParam("id")Long id){
        TbUserAddress tbUserAddress = userAddressService.getById(id);
        UserAddressDTO userAddressDTO = BeanHelper.copyProperties(tbUserAddress, UserAddressDTO.class);
        return ResponseEntity.ok(userAddressDTO);
    }

    @GetMapping("/address/list")
    public ResponseEntity<List<UserAddressDTO>> findUserAddresByUserId(){
        String userId = UserHolder.getUserId();
        List<UserAddressDTO> userAddressDTOS= userAddressService.findUserAddresByUserId(userId);
        return ResponseEntity.ok(userAddressDTOS);
    }
}
