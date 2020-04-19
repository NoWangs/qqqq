package com.leyou.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.leyou.entity.TbOrderDetail;
import com.leyou.mapper.TbOrderDetailMapper;
import com.leyou.service.TbOrderDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 订单详情表 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbOrderDetailServiceImpl extends ServiceImpl<TbOrderDetailMapper, TbOrderDetail> implements TbOrderDetailService {

    @Override
    public List<TbOrderDetail> findOverTimeOrderDetail() {
        return this.getBaseMapper().findOverTimeOrderDetail();

    }
}
