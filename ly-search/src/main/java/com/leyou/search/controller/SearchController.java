package com.leyou.search.controller;

import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.service.SearchService;
import com.leyou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;
    @PostMapping(value = "/page",name = "关键词搜索查询")
    public ResponseEntity<PageResult<GoodsDTO>> findGoodsByPage(@RequestBody SearchRequest searchRequest){
        PageResult<GoodsDTO> pageResult= searchService.findGoodsByPage(searchRequest);
        return ResponseEntity.ok(pageResult);
    }
    @PostMapping(value = "/filter",name = "查询过滤条件")
    public ResponseEntity<Map<String, List<?>>> filter(@RequestBody SearchRequest searchRequest){
        Map<String, List<?>>  filterMap =searchService.filter(searchRequest);
        return ResponseEntity.ok(filterMap);

    }
}
