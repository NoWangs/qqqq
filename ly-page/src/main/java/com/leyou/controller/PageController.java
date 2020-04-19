package com.leyou.controller;

import com.leyou.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class PageController {
    @Autowired
    private PageService pageService;
    @GetMapping("/item/{id}.html")
    //存入model和存入request一样  使用model直接存入map
    public String pageDetail(Model model,@PathVariable("id") Long id){

        Map map=pageService.buildPageBySpuId(id);
        model.addAllAttributes(map);
        return "item";
    }
}
