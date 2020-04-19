package com.leyou;

import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SpuDTO;
import com.leyou.service.PageService;
import com.leyou.vo.PageResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class pageTest {
    @Autowired
    private ItemClient itemClient;
    @Autowired
    private PageService pageService;
    @Autowired
    private TemplateEngine templateEngine;
    @Test
    public void createAllPage(){
        int page=1;
        while(true){
            try{
                PageResult<SpuDTO> spuByPage = itemClient.findSpuByPage(page, 100, null, true);

            if (spuByPage==null||spuByPage.getItems()==null){
                break;
            }
            List<SpuDTO> spulist = spuByPage.getItems();
                for (SpuDTO spuDTO : spulist) {
                    Long spuid = spuDTO.getId();
                    Context context = new Context();
                    context.setVariables(pageService.buildPageBySpuId(spuid));
                    try(PrintWriter writer = new PrintWriter("E:\\java\\nginx\\nginx-1.16.0\\html\\item\\" + spuid + ".html")) {
                            templateEngine.process("item",context,writer);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }catch (Exception e){
                break;
            }
            page++;
        }
    }
}
