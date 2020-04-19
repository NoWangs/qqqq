package com.leyou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyou.entity.TbOrderDetail;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * <p>
 * 订单详情表 Mapper 接口
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
public interface TbOrderDetailMapper extends BaseMapper<TbOrderDetail> {
    @Select(" select od.sku_id,sum(od.num) num from tb_order o,tb_order_detail od where o.order_id = od.order_id and o.STATUS=1 and TIMESTAMPDIFF(MINUTE,o.create_time,NOW()) > 60 GROUP BY od.sku_id")
    List<TbOrderDetail> findOverTimeOrderDetail();
}
