package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.enums.ExceptionEnum;
import com.leyou.exception.LyException;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entity.TbBrand;
import com.leyou.item.entity.TbCategoryBrand;
import com.leyou.item.mapper.TbBrandMapper;
import com.leyou.item.service.TbBrandService;
import com.leyou.item.service.TbCategoryBrandService;
import com.leyou.item.service.TbCategoryService;
import com.leyou.utils.BeanHelper;
import com.leyou.vo.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 品牌表，一个品牌下有多个商品（spu），一对多关系 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbBrandServiceImpl extends ServiceImpl<TbBrandMapper, TbBrand> implements TbBrandService {

    @Override
    public PageResult<BrandDTO> queryBrandByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        QueryWrapper<TbBrand> queryWrapper = new QueryWrapper<>();
        Page<TbBrand> p = new Page<>(page, rows);
        //搜索框
        if (StringUtils.isNoneBlank(key)){
            queryWrapper.lambda().like(TbBrand::getName,key).or().eq(TbBrand::getLetter, key);

        }
        //排序
        if (StringUtils.isNoneBlank(sortBy)){
            if (desc){
                p.setDesc(sortBy);
            }else {
                p.setAsc(sortBy);
            }
        }
        //总页数和当前页数据
        //IPage page1 = this.page(p, queryWrapper);
        IPage<TbBrand> page1 = this.page(p, queryWrapper);
        //page1.getTotal();//总条数
       // page1.getRecords();//当前页的数据
        return new PageResult(page1.getTotal(),page1.getRecords());


    }
    @Autowired
    private TbCategoryBrandService categoryBrandService;
    @Override
    @Transactional
    public void addBrand(BrandDTO brandDTO, List<Long> cids) {
        //两张表  TbBrand插入品牌信息 tb_category_brand插入cids
        //Dto转为TbBrand
        TbBrand tbBrand = BeanHelper.copyProperties(brandDTO, TbBrand.class);
        //插入品牌信息
        this.save(tbBrand);
        //中间表中插入cid
        for (Long cid : cids) {
            TbCategoryBrand categoryBrand = new TbCategoryBrand();
            Long id = tbBrand.getId();
            categoryBrand.setBrandId(id);
            categoryBrand.setCategoryId(cid);
            categoryBrandService.save(categoryBrand);
        }
    }

    @Override
    @Transactional
    public void updateBrand(BrandDTO brandDTO, List<Long> cids) {
        //获取品牌id
        Long brandId = brandDTO.getId();
        //更新品牌表
        TbBrand tbBrand = BeanHelper.copyProperties(brandDTO, TbBrand.class);
        this.updateById(tbBrand);
        //更新分类信息是先清空源数据在写入新数据
        //根据品牌id查询出院数据
        QueryWrapper<TbCategoryBrand> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbCategoryBrand::getBrandId,brandId);
        categoryBrandService.remove(queryWrapper);

        //将新分类的cid和品牌id保存
        for (Long cid : cids) {
            TbCategoryBrand tbCategoryBrand = new TbCategoryBrand();
            tbCategoryBrand.setBrandId(tbBrand.getId());
            tbCategoryBrand.setCategoryId(cid);
            categoryBrandService.save(tbCategoryBrand);
        }
    }
    //根据分类id查询品牌
    @Override
    public List<BrandDTO> findBrandByCategroyId(Long id) {
        List<TbBrand> brandDTOList=this.getBaseMapper().ByCategroyId(id);
        if (CollectionUtils.isEmpty(brandDTOList)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(brandDTOList, BrandDTO.class);
    }

    @Override
    public List<BrandDTO> findBrandListByIds(List<Long> ids) {
        Collection<TbBrand> tbBrands = this.listByIds(ids);
        if (CollectionUtils.isEmpty(tbBrands)){
            throw  new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //集合类型为collection不能直接用Beanhelper
        List<BrandDTO> brandDTOList = tbBrands.stream().map(tbBrand -> {
            return BeanHelper.copyProperties(tbBrand, BrandDTO.class);
        }).collect(Collectors.toList());
        return brandDTOList;
    }

    @Override
    public BrandDTO findBrandByBrangId(Long id) {
        TbBrand tbBrand = this.getById(id);
        BrandDTO brandDTO = BeanHelper.copyProperties(tbBrand, BrandDTO.class);
        return brandDTO;
    }
}
