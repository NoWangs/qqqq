package com.leyou.item.controller;

import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.TbCategory;
import com.leyou.item.service.TbCategoryBrandService;
import com.leyou.item.service.TbCategoryService;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
   private TbCategoryService categoryService;
    @GetMapping("/of/parent")
    public ResponseEntity<List<CategoryDTO>> findByParentId(@RequestParam(name = "pid") Long pid){
        List<CategoryDTO> categoryDTOList=categoryService.findByParentId(pid);
        return ResponseEntity.ok(categoryDTOList);
    }
    @PostMapping
        public void save(@RequestParam("name") String name
                         /*@RequestParam(name = "isParent")Boolean isParent,
                         @RequestParam(name = "sort")Integer sort,
                         @RequestParam(name = "parentId")Long parentId*/
                       ){
        System.out.println(name);

        /*TbCategory tbCategory1 = new TbCategory();
        tbCategory1.setName(name);
        tbCategory1.setIsParent(isParent);
        tbCategory1.setSort(sort);
        tbCategory1.setParentId(parentId);
        tbCategory1.setCreateTime(new Date());
        tbCategory1.setUpdateTime(new Date());*/
         //categoryService.save(tbCategory1);

        //System.out.println(tbCategory);
    }

    //修改品牌信息回显分类信息
    @GetMapping("/of/brand")
    public ResponseEntity<List<CategoryDTO>> findByBrandId(@RequestParam("id") Long id){
       List<CategoryDTO> categoryDTOS= categoryService.findByBrandId(id);
       return ResponseEntity.ok(categoryDTOS);
    }
    @GetMapping(value = "/list",name = "根据分类id集合查询分类对象集合")
    public ResponseEntity<List<CategoryDTO>> findCategoryByIds(@RequestParam("ids") List<Long> ids){
        List<CategoryDTO> categoryDTOList= categoryService.findCategoryByIds(ids);
        return ResponseEntity.ok(categoryDTOList);
    }
}
