package com.leyou.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.enums.ExceptionEnum;
import com.leyou.exception.LyException;
import com.leyou.user.dto.UserAddressDTO;
import com.leyou.user.entity.TbUserAddress;
import com.leyou.user.mapper.TbUserAddressMapper;
import com.leyou.user.service.TbUserAddressService;
import com.leyou.utils.BeanHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <p>
 * 用户收货地址表 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbUserAddressServiceImpl extends ServiceImpl<TbUserAddressMapper, TbUserAddress> implements TbUserAddressService {

    @Override
    public List<UserAddressDTO> findUserAddresByUserId(String userId) {
        QueryWrapper<TbUserAddress> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbUserAddress::getUserId,userId);
        List<TbUserAddress> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.USER_ADDRESS_NOT_FOUND);
        }
        List<UserAddressDTO> userAddressDTOS = BeanHelper.copyWithCollection(list, UserAddressDTO.class);
        System.out.println(userAddressDTOS);
        return userAddressDTOS;
    }
}
