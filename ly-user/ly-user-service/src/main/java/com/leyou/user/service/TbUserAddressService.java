package com.leyou.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.user.dto.UserAddressDTO;
import com.leyou.user.entity.TbUserAddress;

import java.util.List;

/**
 * <p>
 * 用户收货地址表 服务类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
public interface TbUserAddressService extends IService<TbUserAddress> {

    List<UserAddressDTO> findUserAddresByUserId(String userId);
}
