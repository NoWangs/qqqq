package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.enums.ExceptionEnum;
import com.leyou.exception.LyException;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.TbCategory;
import com.leyou.item.mapper.TbCategoryMapper;
import com.leyou.item.service.TbCategoryService;
import com.leyou.utils.BeanHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 商品类目表，类目和商品(spu)是一对多关系，类目与品牌是多对多关系 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbCategoryServiceImpl extends ServiceImpl<TbCategoryMapper, TbCategory> implements TbCategoryService {

    @Override
    public List<CategoryDTO> findByParentId(Long pid) {
        QueryWrapper<TbCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbCategory::getParentId, pid);
        List<TbCategory> tbCategoryList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(tbCategoryList)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        List<CategoryDTO> categoryDTOS = BeanHelper.copyWithCollection(tbCategoryList, CategoryDTO.class);
        return categoryDTOS;
    }
    //修改品牌是分类的信息回显
    @Override
    public List<CategoryDTO> findByBrandId(Long id) {
        List<CategoryDTO> categoryDTOList=this.getBaseMapper().findByBrandId(id);
        if (CollectionUtils.isEmpty(categoryDTOList)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        List<CategoryDTO> categoryDTOS = BeanHelper.copyWithCollection(categoryDTOList, CategoryDTO.class);

        return categoryDTOS;
    }

    @Override
    public List<CategoryDTO> findCategoryByIds(List<Long> ids) {

        Collection<TbCategory> categoryList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(categoryList)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        List<CategoryDTO> categoryDTOList = categoryList.stream().map(tbCategory -> {
            return BeanHelper.copyProperties(tbCategory, CategoryDTO.class);
        }).collect(Collectors.toList());

        return categoryDTOList;
    }
}
