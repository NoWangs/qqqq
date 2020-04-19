package com.leyou.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entity.TbBrand;
import com.leyou.vo.PageResult;

import java.util.List;

/**
 * <p>
 * 品牌表，一个品牌下有多个商品（spu），一对多关系 服务类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
public interface TbBrandService extends IService<TbBrand> {

    PageResult<BrandDTO> queryBrandByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc);

    void addBrand(BrandDTO brandDTO, List<Long> cids);

    void updateBrand(BrandDTO brandDTO, List<Long> cids);

    List<BrandDTO> findBrandByCategroyId(Long id);

    List<BrandDTO> findBrandListByIds(List<Long> ids);

    BrandDTO findBrandByBrangId(Long id);
}
