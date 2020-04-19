package com.leyou.item.controller;

import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entity.TbBrand;
import com.leyou.item.service.TbBrandService;
import com.leyou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandController {
        @Autowired
        private TbBrandService tbBrandService;


        @GetMapping("/page")//品牌列表查询
    public ResponseEntity<PageResult<BrandDTO>> queryBrandByPage(@RequestParam(value = "key",required = false) String key,
                                                                 @RequestParam(value = "page",defaultValue = "1") Integer page,
                                                                 @RequestParam(value = "rows",defaultValue = "5") Integer rows,
                                                                 @RequestParam(value = "sortBy",required = false) String sortBy,
                                                                 @RequestParam(value = "desc",defaultValue = "false") Boolean desc){
        PageResult<BrandDTO> pageResult=tbBrandService.queryBrandByPage(key,page,rows,sortBy,desc);
        return ResponseEntity.ok(pageResult);


    }

    //新增品牌
    @PostMapping
    public ResponseEntity<Void> addBrand(BrandDTO brandDTO,@RequestParam("cids") List<Long> cids){
            tbBrandService.addBrand(brandDTO,cids);
            return ResponseEntity.ok().build();
    }

    //修改品牌
    @PutMapping
    public ResponseEntity<Void> updateBrand(BrandDTO brandDTO,@RequestParam("cids") List<Long> cids){
        tbBrandService.updateBrand(brandDTO,cids);
        return ResponseEntity.ok().build();
    }
    @GetMapping(value = "/of/category",name = "根据分类id查询品牌")
    public ResponseEntity<List<BrandDTO>> findBrandByCategroyId(@RequestParam("id")Long id){
        List<BrandDTO> brandDTOList= tbBrandService.findBrandByCategroyId(id);
        return ResponseEntity.ok(brandDTOList);
    }
    @GetMapping(value = "/list",name = "根据品牌id集合查询品牌对象集合")
    public ResponseEntity<List<BrandDTO>> findBrandListByIds(@RequestParam("ids") List<Long> ids){
        List<BrandDTO> brandDTOList= tbBrandService.findBrandListByIds(ids);
        return ResponseEntity.ok(brandDTOList);
    }
    @GetMapping(value = "/{id}",name = "根据品牌id查询品牌信息")
    public ResponseEntity<BrandDTO> findBrandByBrangId(@PathVariable("id")Long id){
            BrandDTO brandDTO=tbBrandService.findBrandByBrangId(id);
            return ResponseEntity.ok(brandDTO);
    }
}
