package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyou.enums.ExceptionEnum;
import com.leyou.exception.LyException;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;
import com.leyou.item.entity.*;
import com.leyou.item.service.*;
import com.leyou.utils.BeanHelper;
import com.leyou.vo.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.leyou.constants.RocketMQConstants.TAGS.ITEM_DOWN_TAGS;
import static com.leyou.constants.RocketMQConstants.TAGS.ITEM_UP_TAGS;
import static com.leyou.constants.RocketMQConstants.TOPIC.ITEM_TOPIC_NAME;

@Service
public class GoodsService {
    @Autowired
    private TbSpuService tbSpuService;
    @Autowired
    private TbCategoryService categoryService;
    @Autowired
    private TbBrandService brandService;
    @Autowired
    private TbSpuService spuService;
    @Autowired
    private TbSpuDetailService spuDetailService;
    @Autowired
    private TbSkuService skuService;
   
    public PageResult<SpuDTO> findSpuByPage(Integer page, Integer rows, String key, Boolean saleable) {
        //分页
        Page<TbSpu> page1 = new Page<>(page, rows);
        QueryWrapper<TbSpu> queryWrapper = new QueryWrapper<>();
        //搜索框
        if (StringUtils.isNoneBlank(key)){
            queryWrapper.lambda().like(TbSpu::getName,key);
        }
        //上下架
        if (saleable!=null){
            queryWrapper.lambda().eq(TbSpu::getSaleable,saleable);
        }
        //分页数据
        IPage<TbSpu> tbSpuIPage = tbSpuService.page(page1, queryWrapper);
        List<TbSpu> records = tbSpuIPage.getRecords();
        List<SpuDTO> spuDTOS = BeanHelper.copyWithCollection(records, SpuDTO.class);
        for (SpuDTO spuDTO : spuDTOS) {
            CategoryAndBrandName(spuDTO);
        }
        return new PageResult(tbSpuIPage.getTotal(),spuDTOS);
    }
    //拿到分类name和品牌name的方法
    private SpuDTO CategoryAndBrandName(SpuDTO spuDTO){
        //根据品牌id查询
        TbBrand tbBrand = brandService.getById(spuDTO.getBrandId());
        spuDTO.setBrandName(tbBrand.getName());
        //获取cids
        List<Long> categoryIds = spuDTO.getCategoryIds();
        //直接根据分类id查出分类的对象
        Collection<TbCategory> tbCategoryCollection = categoryService.listByIds(categoryIds);
        //获取每个对象的名称  用/隔开
        String categoryName = tbCategoryCollection.stream()//变成流
                .map(TbCategory::getName)//获取每个对象的名称
                .collect(Collectors.joining("/"));//用/隔开

        spuDTO.setCategoryName(categoryName);
        return spuDTO;
    }

    public void addGoods(SpuDTO spuDTO) {
        //保存到3张表 spu sku spudetial
        //转为TbSpu 将spu存入数据库
        TbSpu tbSpu = BeanHelper.copyProperties(spuDTO, TbSpu.class);
        spuService.save(tbSpu);
        //拿到spuid以备后用
        Long spuId = tbSpu.getId();
        //实体类加入SpuDetail  拿到SpuDetail转为TbSpuDetail
        TbSpuDetail tbSpuDetail = BeanHelper.copyProperties(spuDTO.getSpuDetail(), TbSpuDetail.class);
        //spuid需要存入数据库
        tbSpuDetail.setSpuId(spuId);
        spuDetailService.save(tbSpuDetail);
        //实体类加入Skus  拿到skus转为TbSku
        List<TbSku> tbSkus = BeanHelper.copyWithCollection(spuDTO.getSkus(), TbSku.class);
        for (TbSku skus : tbSkus) {
            //拿到每一个sku设置spuid
            skus.setSpuId(spuId);
        }
        //批量保存sku
        skuService.saveBatch(tbSkus);
    }
    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;
    //商品上下架
    public void updateSaleable(Long id, Boolean saleable) {
        TbSpu tbSpu = new TbSpu();
        tbSpu.setId(id);
        tbSpu.setSaleable(saleable);
        boolean b = spuService.updateById(tbSpu);
        if (!b){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        UpdateWrapper<TbSku> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(TbSku::getSpuId,id);
        updateWrapper.lambda().set(TbSku::getEnable,saleable);
        boolean update = skuService.update(updateWrapper);
        if (!update){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //上下架
        //向mq中发消息
        // true的话上架false的话下架   ITEM_UP_TAGS上架   ITEM_DOWN_TAGS下架
        String tag = saleable ? ITEM_UP_TAGS : ITEM_DOWN_TAGS;

        rocketMQTemplate.convertAndSend(ITEM_TOPIC_NAME+":"+tag,id);

        System.out.println("上下架成功,MQ消息已发出"+tag);

    }

    public SpuDetailDTO findSpuDetail(Long id) {
       /* QueryWrapper<TbSpuDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSpuDetail::getSpuId,id);*/
        TbSpuDetail spuDetail = spuDetailService.getById(id);
        SpuDetailDTO spuDetailDTO = BeanHelper.copyProperties(spuDetail, SpuDetailDTO.class);
        if (spuDetailDTO==null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
       /* List<TbSpuDetail> list = spuDetailService.list(queryWrapper);
        List<SpuDetailDTO> spuDetailDTOList = BeanHelper.copyWithCollection(list, SpuDetailDTO.class);*/
        return spuDetailDTO;
    }

    public List<SkuDTO> findSku(Long id) {
        QueryWrapper<TbSku> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSku::getSpuId,id);
        List<TbSku> skuList = skuService.list(queryWrapper);
        if (CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        List<SkuDTO> skuDTOList = BeanHelper.copyWithCollection(skuList, SkuDTO.class);
        return skuDTOList;
    }

    public void updateGoods(SpuDTO spuDTO) {
        //保存到3张表 spu sku spudetial
        //转为TbSpu 将spu存入数据库
        TbSpu tbSpu = BeanHelper.copyProperties(spuDTO, TbSpu.class);
        spuService.updateById(tbSpu);
        //拿到spuid以备后用
        Long spuId = tbSpu.getId();
        //实体类加入SpuDetail  拿到SpuDetail转为TbSpuDetail
        TbSpuDetail tbSpuDetail = BeanHelper.copyProperties(spuDTO.getSpuDetail(), TbSpuDetail.class);
       spuDetailService.updateById(tbSpuDetail);
        //先将sku删除  然后在新增
        UpdateWrapper<TbSku> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(TbSku::getSpuId,spuId);
        skuService.remove(updateWrapper);
        //实体类加入Skus  拿到skus转为TbSku
        List<TbSku> tbSkus = BeanHelper.copyWithCollection(spuDTO.getSkus(), TbSku.class);
        for (TbSku skus : tbSkus) {
            //拿到每一个sku设置spuid
            skus.setSpuId(spuId);
        }
        //批量保存sku
        skuService.saveBatch(tbSkus);
    }

    public SpuDTO findSpuBySpuId(Long id) {
        TbSpu tbSpu = spuService.getById(id);
        SpuDTO spuDTO = BeanHelper.copyProperties(tbSpu, SpuDTO.class);
        return spuDTO;
    }

    public List<SkuDTO> findSkuListByIds(List<Long> ids) {
        Collection<TbSku> tbSkus = skuService.listByIds(ids);
        List<SkuDTO> skuDTOS = tbSkus.stream().map(tbSku -> {
            return BeanHelper.copyProperties(tbSku, SkuDTO.class);
        }).collect(Collectors.toList());
        return skuDTOS;
    }

    public void stockMinus(Map<Long, Integer> skuIdAndNumMap) {

        skuService.stockMinus(skuIdAndNumMap);
    }

    public void stockPlus(Map<Long, Integer> skuIdAndNumMap) {
        skuService.stockPlus(skuIdAndNumMap);
    }

    public List<SpuDTO> findSpuByBrandIdAndCid(Long brandId, Long cid) {
        QueryWrapper<TbSpu> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSpu::getBrandId,brandId).eq(TbSpu::getCid3,cid);
        List<TbSpu> list = spuService.list(queryWrapper);
        List<SpuDTO> spuDTOS = BeanHelper.copyWithCollection(list, SpuDTO.class);
        return spuDTOS;
    }
}
