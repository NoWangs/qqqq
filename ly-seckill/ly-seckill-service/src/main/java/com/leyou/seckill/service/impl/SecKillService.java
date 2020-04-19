package com.leyou.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.leyou.enums.ExceptionEnum;
import com.leyou.exception.LyException;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.seckill.dto.SeckillPolicyDTO;

import com.leyou.seckill.ectity.TbSeckillPolicy;
import com.leyou.seckill.service.TbSeckillPolicyService;
import com.leyou.utils.BeanHelper;
import com.leyou.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 秒杀管理平台业务
 * @author
 */
@Slf4j
@Service
public class SecKillService {
    @Autowired
    private TbSeckillPolicyService tbSeckillPolicyService;
    @Autowired
    private ItemClient itemClient;

    /**
     * 分页查询 秒杀信息
     *
     * @param page
     * @param rows
     * @param key
     * @param state 1-未开始 2-进行中  3-已结束
     * @return
     */
    public PageResult<SeckillPolicyDTO> findSecKillByPage(Integer page, Integer rows, String key, Integer state) {

        IPage<TbSeckillPolicy> page1 = new Page<>(page, rows);
        QueryWrapper<TbSeckillPolicy> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.lambda().like(TbSeckillPolicy::getTitle, key);
        }
        if (state != null) {
            Date now = new Date();
            switch (state) {
                case 1:
                    queryWrapper.lambda().gt(TbSeckillPolicy::getBeginTime, now);
                    break;
                case 2:
                    queryWrapper.lambda().lt(TbSeckillPolicy::getBeginTime, now).gt(TbSeckillPolicy::getEndTime, now);
                    break;
                case 3:
                    queryWrapper.lambda().lt(TbSeckillPolicy::getEndTime, now);
                    break;

            }
        }
        queryWrapper.orderByAsc("begin_time");
        IPage<TbSeckillPolicy> policyIPage = tbSeckillPolicyService.page(page1, queryWrapper);
        if (policyIPage == null || CollectionUtils.isEmpty(policyIPage.getRecords())) {
            throw new LyException(ExceptionEnum.SECKILL_NOT_FOUND);
        }

        List<TbSeckillPolicy> tbSeckillPolicyList = policyIPage.getRecords();
        List<SeckillPolicyDTO> seckillPolicyDTOS = BeanHelper.copyWithCollection(tbSeckillPolicyList, SeckillPolicyDTO.class);
        this.handleCategoryAndBrandName(seckillPolicyDTOS);
        return new PageResult<SeckillPolicyDTO>( policyIPage.getTotal(), policyIPage.getPages(),seckillPolicyDTOS);
    }

    /**
     * 处理 品牌名称 和 分类名称
     *
     * @param seckillPolicyDTOS
     */
    private void handleCategoryAndBrandName(List<SeckillPolicyDTO> seckillPolicyDTOS) {

        for (SeckillPolicyDTO seckillPolicyDTO : seckillPolicyDTOS) {
            //通过brandId查询brandName
            Long brandId = seckillPolicyDTO.getBrandId();
            BrandDTO brandDTO = itemClient.findBrandByBrangId(brandId);
            seckillPolicyDTO.setBrandName(brandDTO.getName());
            //查询categoryName  结果如 ：手机/手机通讯/手机    1级分类名/2级分类名/3级分类名
            List<Long> cids = seckillPolicyDTO.getCategorys();
            List<CategoryDTO> categoryDTOList = itemClient.findCategoryByIds(cids);
            String categoryNames = categoryDTOList.stream().map(CategoryDTO::getName).collect(Collectors.joining("/"));
            seckillPolicyDTO.setCategoryName(categoryNames);
        }
    }

    /**
     * 根据id 查询 秒杀信息
     *
     * @param id
     * @return
     */
    public SeckillPolicyDTO findSecKillById(Long id) {
        TbSeckillPolicy tbSeckillPolicy = tbSeckillPolicyService.getById(id);
        if (tbSeckillPolicy == null) {
            throw new LyException(ExceptionEnum.SECKILL_NOT_FOUND);
        }
        return BeanHelper.copyProperties(tbSeckillPolicy, SeckillPolicyDTO.class);
    }

    /**
     * 添加秒杀商品信息
     *
     * @param seckillPolicy
     */
    public void addSecKill(TbSeckillPolicy seckillPolicy) {
        //获取秒杀日期 yyyy-MM-dd
        String secKillDay = new DateTime(seckillPolicy.getBeginTime()).toString("yyyy-MM-dd");
//        设置秒杀库存
        seckillPolicy.setStockCount(seckillPolicy.getNum());
//        设置秒杀 结束时间，默认为 开始2个小时后结束
        Date endTime = new DateTime(seckillPolicy.getBeginTime()).plusHours(2).toDate();
        seckillPolicy.setEndTime(endTime);
//        设置秒杀 日期
        seckillPolicy.setSecKillDate(secKillDay);
        boolean b = tbSeckillPolicyService.save(seckillPolicy);
        if (!b) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

    }

    /**
     * 修改秒杀信息
     *
     * @param seckillPolicy
     */
    public void updateSecKill(TbSeckillPolicy seckillPolicy) {
        //获取秒杀日期 yyyy-MM-dd
        String secKillDay = new DateTime(seckillPolicy.getBeginTime()).toString("yyyy-MM-dd");
        // 设置秒杀库存
        seckillPolicy.setStockCount(seckillPolicy.getNum());
//        设置秒杀 结束时间，默认为 开始2个小时后结束
        Date endTime = new DateTime(seckillPolicy.getBeginTime()).plusHours(2).toDate();
        seckillPolicy.setEndTime(endTime);
        //        设置秒杀 日期
        seckillPolicy.setSecKillDate(secKillDay);
        boolean b = tbSeckillPolicyService.updateById(seckillPolicy);
        if (!b) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

    }

    /**
     * 删除 秒杀信息
     *
     * @param id
     */
    public void deleteSecKill(Long id) {
//        先把要删除的数据查询出来
        TbSeckillPolicy tbSeckillPolicy = tbSeckillPolicyService.getById(id);
        boolean b = tbSeckillPolicyService.removeById(id);
        if (!b) {
            throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }
    }

    public List<SeckillPolicyDTO> findSecKillPolicyList(String date) {
        QueryWrapper<TbSeckillPolicy> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSeckillPolicy::getSecKillDate,date );
        List<TbSeckillPolicy> list = tbSeckillPolicyService.list(queryWrapper);
        List<SeckillPolicyDTO> seckillPolicyDTOS = BeanHelper.copyWithCollection(list, SeckillPolicyDTO.class);
        return seckillPolicyDTOS;
    }
}
