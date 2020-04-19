package com.leyou.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.TbUser;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
public interface TbUserService extends IService<TbUser> {

    Boolean checkUserData(String data, Integer type);

    void sendCode(String phone);

    void register(TbUser tbUser, String code);

    UserDTO queryUsernameAndPassword(String username, String password);
}
