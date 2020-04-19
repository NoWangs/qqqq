package com.leyou.service;

import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.*;
import com.leyou.seckill.client.SeckillClient;
import com.leyou.seckill.dto.SeckillPolicyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageService {
    @Autowired
    private ItemClient itemClient;

    public Map buildPageBySpuId(Long id) {
        //返回一个map先new一个map
        Map mapData=new HashMap();
        //查询spu拿到名字,子标题
        SpuDTO spu = itemClient.findSpuBySpuId(id);
        String spuName = spu.getName();
        mapData.put("spuName", spuName);
        String subTitle = spu.getSubTitle();
        mapData.put("subTitle", subTitle);
        //拿到3级分类的id
        List<Long> categoryIds = spu.getCategoryIds();
        //查询出三级分类的集合
        List<CategoryDTO> categoryList = itemClient.findCategoryByIds(categoryIds);
        mapData.put("categories", categoryList);
        //拿到品牌的对象
        Long brandId = spu.getBrandId();
        BrandDTO brand = itemClient.findBrandByBrangId(brandId);
        mapData.put("brand", brand);
        //拿到spudetail对象
        SpuDetailDTO spuDetail = itemClient.findSpuDetail(id);
        mapData.put("detail", spuDetail);
        //skus
        List<SkuDTO> skus = itemClient.findSku(id);
        mapData.put("skus",skus);
        //spexs
        List<SpecGroupDTO> specGroupDTOS = itemClient.findSpecGroupWithSpecParamByCategoryId(spu.getCid3());
        mapData.put("specs", specGroupDTOS);


        return mapData;
    }
    @Autowired
    private TemplateEngine templateEngine;
    public void createHtml(Long spuId) {

        Context context = new Context();
        context.setVariables(this.buildPageBySpuId(spuId));
        try(PrintWriter writer = new PrintWriter("E:\\java\\nginx\\nginx-1.16.0\\html\\item\\" + spuId + ".html")) {
            templateEngine.process("item",context,writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("静态页面面添加成功");
    }

    public void removeHtml(Long spuId) {

        File file = new File("E:\\java\\nginx\\nginx-1.16.0\\html\\item\\" + spuId + ".html");
        file.delete();
        System.out.println("静态页面删除成功");
    }
    @Autowired
    private SeckillClient seckillClient;

    public void createSeckillHtml(String date) {

        List<SeckillPolicyDTO> secKillPolicyList = seckillClient.findSecKillPolicyList(date);
        createSeckillList(secKillPolicyList);

        for (SeckillPolicyDTO seckillPolicyDTO : secKillPolicyList) {
            createSeckillGood(seckillPolicyDTO);
        }

        System.out.println("q");

    }

    private void createSeckillGood(SeckillPolicyDTO seckillPolicyDTO) {
        Long seckillId = seckillPolicyDTO.getId();
        Long spuId = seckillPolicyDTO.getSpuId();
        SpuDetailDTO spuDetail = itemClient.findSpuDetail(spuId);
        Context context = new Context();
        context.setVariable("detail", spuDetail);
        context.setVariable("seckillgoods",seckillPolicyDTO);
        List<SpecGroupDTO> specList = itemClient.findSpecGroupWithSpecParamByCategoryId(seckillPolicyDTO.getCid3());
        context.setVariable("specs",specList);
        try(PrintWriter printWriter=new PrintWriter("E:\\java\\nginx\\nginx-1.16.0\\html\\seckill\\"+seckillId+".html")){
            templateEngine.process("seckill-item", context,printWriter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void createSeckillList(List<SeckillPolicyDTO> secKillPolicyList) {
        Context context = new Context();
        context.setVariable("seckillList",secKillPolicyList);
        try(PrintWriter printWriter=new PrintWriter("E:\\java\\nginx\\nginx-1.16.0\\html\\seckill\\list.html")){
            templateEngine.process("seckill-index", context,printWriter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
