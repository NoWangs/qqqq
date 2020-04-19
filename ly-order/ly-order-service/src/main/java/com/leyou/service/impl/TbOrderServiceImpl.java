package com.leyou.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.leyou.entity.TbOrder;
import com.leyou.mapper.TbOrderMapper;
import com.leyou.service.TbOrderService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbOrderServiceImpl extends ServiceImpl<TbOrderMapper, TbOrder> implements TbOrderService {

    @Override
    public void updateOverTimeOrderStutas() {
        this.getBaseMapper().updateOverTimeOrderStutas();
    }
}
