package com.leyou.item.controller;

import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;
import com.leyou.item.service.impl.GoodsService;
import com.leyou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;
    @GetMapping(value = "/spu/page",name = "分页查询商品信息")
    public ResponseEntity<PageResult<SpuDTO>> findSpuByPage(@RequestParam(value = "page",defaultValue = "1") Integer page,
                                                            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
                                                            @RequestParam(value = "key",required = false)String key,
                                                            @RequestParam(value = "saleable",required = false)Boolean saleable){

        PageResult<SpuDTO> pageResult=goodsService.findSpuByPage(page,rows,key,saleable);
        return ResponseEntity.ok(pageResult);
    }
    @PostMapping(value = "/goods",name = "新增商品信息")
    public ResponseEntity<Void> addGoods(@RequestBody SpuDTO spuDTO){
        goodsService.addGoods(spuDTO);
        return ResponseEntity.ok().build();
    }
    @PutMapping(value = "/spu/saleable",name = "修改商品上下架")
    public ResponseEntity<Void> updateSaleable(@RequestParam("id") Long id,
                                               @RequestParam("saleable") Boolean saleable){
        goodsService.updateSaleable(id,saleable);
        return ResponseEntity.ok().build();
    }
    @GetMapping(value = "/spu/detail",name = "修改商品信息回显spudetail")
    public ResponseEntity<SpuDetailDTO> findSpuDetail(@RequestParam("id")Long id){
        SpuDetailDTO spuDetail = goodsService.findSpuDetail(id);
        return ResponseEntity.ok(spuDetail);
    }
    @GetMapping(value = "/sku/of/spu",name = "修改商品信息回显sku")
    public ResponseEntity<List<SkuDTO>> findSku(@RequestParam("id")Long id){
        List<SkuDTO> skuDTOList=goodsService.findSku(id);
        return ResponseEntity.ok(skuDTOList);
    }
    @PutMapping(value = "/goods",name = "修改商品信息")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuDTO spuDTO){
        goodsService.updateGoods(spuDTO);
        return ResponseEntity.ok().build();
    }
    @GetMapping(value = "/spu/{id}",name = "根据spuid查询spu信息")
    public ResponseEntity<SpuDTO> findSpuBySpuId(@PathVariable("id")Long id){
       SpuDTO spuDTO= goodsService.findSpuBySpuId(id);
        return ResponseEntity.ok(spuDTO);
    }
    @GetMapping(value = "/sku/list",name = "根据ids查询sku集合")
    public ResponseEntity<List<SkuDTO>> findSkuListByIds(@RequestParam("ids")List<Long> ids){
        List<SkuDTO> skuDTOS=goodsService.findSkuListByIds(ids);
        return ResponseEntity.ok(skuDTOS);
    }

    @PutMapping(value = "/stock/minus",name = "减库存")
    public ResponseEntity<Void> stockMinus(@RequestBody Map<Long,Integer> skuIdAndNumMap){
        goodsService.stockMinus(skuIdAndNumMap);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/stock/plus",name = "加库存")
    public ResponseEntity<Void> stockPlus(@RequestBody Map<Long,Integer> skuIdAndNumMap){
        goodsService.stockPlus(skuIdAndNumMap);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/spu/for/brand")
    public ResponseEntity<List<SpuDTO>> findSpuByBrandIdAndCid(@RequestParam("id")Long brandId,
                                                               @RequestParam("cid")Long cid){
        List<SpuDTO> spuDTOS= goodsService.findSpuByBrandIdAndCid(brandId,cid);
        return ResponseEntity.ok(spuDTOS);
    }
}
