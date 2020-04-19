package com.leyou.item.client;

import com.leyou.item.dto.*;
import com.leyou.vo.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient("item-service")
public interface ItemClient {

    @GetMapping(value = "/brand/of/category",name = "根据分类id查询品牌")
    public List<BrandDTO> findBrandByCategroyId(@RequestParam("id")Long id);

    @GetMapping(value = "/spu/page",name = "分页查询商品信息")
    public PageResult<SpuDTO> findSpuByPage(@RequestParam(value = "page",defaultValue = "1") Integer page,
                                                            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
                                                            @RequestParam(value = "key",required = false)String key,
                                                            @RequestParam(value = "saleable",required = false)Boolean saleable);

    @GetMapping(value = "/spu/detail",name = "修改商品信息回显spudetail")
    public SpuDetailDTO findSpuDetail(@RequestParam("id")Long id);

    @GetMapping(value = "/sku/of/spu",name = "修改商品信息回显sku")
    public List<SkuDTO> findSku(@RequestParam("id")Long id);

    @GetMapping("/spec/params")
    public List<SpecParamDTO> findSpecParamByGid(@RequestParam(value = "gid",required = false)Long gid,
                                                 @RequestParam(value = "cid",required = false)Long cid,
                                                 @RequestParam(value ="searching",required = false)Boolean searching);

    @GetMapping(value = "/brand/list",name = "根据品牌id集合查询品牌对象集合")
    public List<BrandDTO> findBrandListByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping(value = "/category/list",name = "根据分类id集合查询分类对象集合")
    public List<CategoryDTO> findCategoryByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping(value = "/spu/{id}",name = "根据spuid查询spu信息")
    public SpuDTO findSpuBySpuId(@PathVariable("id")Long id);

    @GetMapping(value = "/brand/{id}",name = "根据品牌id查询品牌信息")
    public BrandDTO findBrandByBrangId(@PathVariable("id")Long id);

    @GetMapping(value = "/spec/of/category",name = "根据categoryId查询规格参数组和组内参数")
    public List<SpecGroupDTO> findSpecGroupWithSpecParamByCategoryId(@RequestParam("id")Long id);

    @GetMapping(value = "/sku/list",name = "根据ids查询sku集合")
    public List<SkuDTO> findSkuListByIds(@RequestParam("ids")List<Long> ids);

    @PutMapping(value = "/stock/minus",name = "减库存")
    public Void stockMinus(@RequestBody Map<Long,Integer> skuIdAndNumMap);

    @PutMapping(value = "/stock/plus",name = "加库存")
    public Void stockPlus(@RequestBody Map<Long,Integer> skuIdAndNumMap);
}
