package com.leyou.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.entity.TbOrder;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
public interface TbOrderService extends IService<TbOrder> {

    void updateOverTimeOrderStutas();
}
