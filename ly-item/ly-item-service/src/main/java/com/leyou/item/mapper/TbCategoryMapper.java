package com.leyou.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.TbCategory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 商品类目表，类目和商品(spu)是一对多关系，类目与品牌是多对多关系 Mapper 接口
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */

public interface TbCategoryMapper extends BaseMapper<TbCategory> {
    @Select("SELECT * FROM tb_category_brand cb,tb_category c WHERE cb.category_id=c.id AND cb.brand_id=#{id}")
    List<CategoryDTO> findByBrandId(@Param("id") Long id);
}
