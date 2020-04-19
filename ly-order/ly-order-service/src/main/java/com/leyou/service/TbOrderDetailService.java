package com.leyou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.entity.TbOrderDetail;

import java.util.List;


/**
 * <p>
 * 订单详情表 服务类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
public interface TbOrderDetailService extends IService<TbOrderDetail> {

    List<TbOrderDetail> findOverTimeOrderDetail();

}
