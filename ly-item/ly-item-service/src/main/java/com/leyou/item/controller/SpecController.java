package com.leyou.item.controller;

import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.service.TbSpecGroupService;
import com.leyou.item.service.TbSpecParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/spec")
public class SpecController {
    @Autowired
    private TbSpecGroupService specGroupService;
    @Autowired
    private TbSpecParamService specParamService;
    @GetMapping(value = "/groups/of/category",name = "根据分类查询参数组信息")
    public ResponseEntity<List<SpecGroupDTO>> findSpecGroupByCid(@RequestParam("id")Long id){
        List<SpecGroupDTO> specGroupDTOList=specGroupService.findSpecGroupByCid(id);
        return ResponseEntity.ok(specGroupDTOList);
    }
    @GetMapping("/params")
    public ResponseEntity<List<SpecParamDTO>> findSpecParamByGid(@RequestParam(value = "gid",required = false)Long gid,
                                                                 @RequestParam(value = "cid",required = false)Long cid,
                                                                 @RequestParam(value ="searching",required = false)Boolean searching){
        List<SpecParamDTO> specParamDTOList=specParamService.findSpecParamBygid(gid,cid,searching);
       // List<SpecParamDTO> specParamDTOList=specParamService.findSpecParamByGid(gid,cid,searching);
        return ResponseEntity.ok(specParamDTOList);
    }
    @GetMapping(value = "/of/category",name = "根据categoryId查询规格参数组和组内参数")
    public ResponseEntity<List<SpecGroupDTO>> findSpecGroupWithSpecParamByCategoryId(@RequestParam("id")Long id){
        List<SpecGroupDTO> specGroupDTOList= specParamService.findSpecGroupWithSpecParamByCategoryId(id);
        return ResponseEntity.ok(specGroupDTOList);
    }
}
