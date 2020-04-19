package com.leyou.search.test;

import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.BrandDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FeignTest {

    @Autowired
    private ItemClient itemClient;
    @Test
    public void testFeign(){
        List<BrandDTO> brandByCategroyId = itemClient.findBrandByCategroyId(76L);
        for (BrandDTO brandDTO : brandByCategroyId) {
            System.out.println(brandDTO);
        }
    }

    @Test
    public void test(){
        ArrayList<Long> longs = new ArrayList<>();
        longs.add(0,1L);
        longs.add(1,1L);
        longs.add(2,1L);
        System.out.println(longs);
        Long aLong = longs.get(0);
        System.out.println(aLong);
    }


}

