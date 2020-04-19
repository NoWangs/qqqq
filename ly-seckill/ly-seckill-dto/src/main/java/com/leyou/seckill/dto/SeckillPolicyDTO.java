package com.leyou.seckill.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SeckillPolicyDTO {
    private Long id;
    private Long spuId;
    private String name;
    private Long cid1;
    private Long cid2;
    private Long cid3;
    private Long brandId;
    private Long skuId;
    private String title;
    private String subTitle;
    private Integer num;
    private Integer stockCount;
    private String skuPic;
    private Long oldPrice;
    private Long costPrice;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date beginTime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date endTime;
    private String secKillDate;
    private String brandName;
    private String categoryName;
    @JsonIgnore
    public List<Long> getCategorys() {
        return Arrays.asList(cid1, cid2, cid3);
    }

}
