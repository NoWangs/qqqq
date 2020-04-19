package com.leyou.search.test;

import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SpuDTO;
import com.leyou.search.entity.Goods;
import com.leyou.search.repository.GoodsRepository;


import com.leyou.search.service.SearchService;
import com.leyou.vo.PageResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ElasticSearchInitTest {
    @Autowired
    private ItemClient itemClient;
    @Autowired
    private SearchService searchService;
    @Autowired
    private GoodsRepository goodsRepository;
    @Test
    public void initEs(){
        goodsRepository.deleteAll();
        //写个死循环每次查100条,无数据位置
        int page=1;
        while(true){
            //查出所有spu
            PageResult<SpuDTO> spu = itemClient.findSpuByPage(page, 100, null, true);
            if (CollectionUtils.isEmpty(spu.getItems())){
                break;//无数据后跳出循环
            }
            //拿到所有的spu
            List<SpuDTO> spuDTOList = spu.getItems();
            //System.out.println(spuDTOList);
            //构建集合存入goods以便批量保存
            List<Goods> list = new ArrayList<>();
            for (SpuDTO spuDTO : spuDTOList) {
                //将spu转为goods
               Goods goods = searchService.buildGoods(spuDTO);
                list.add(goods);
            }
            //System.out.println(list);
            //将goods保存到es中
            goodsRepository.saveAll(list);
           page++;
        }
    }

}
