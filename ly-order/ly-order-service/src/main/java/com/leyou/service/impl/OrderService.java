package com.leyou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.auth.entity.UserHolder;
import com.leyou.config.PayProperties;
import com.leyou.entity.TbOrder;
import com.leyou.entity.TbOrderDetail;
import com.leyou.entity.TbOrderLogistics;
import com.leyou.enums.BusinessTypeEnum;
import com.leyou.enums.ExceptionEnum;
import com.leyou.enums.OrderStatusEnum;
import com.leyou.exception.LyException;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SkuDTO;
import com.leyou.order.dto.CartDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.vo.OrderDetailVO;
import com.leyou.order.vo.OrderLogisticsVO;
import com.leyou.order.vo.OrderVO;
import com.leyou.service.TbOrderDetailService;
import com.leyou.service.TbOrderLogisticsService;
import com.leyou.service.TbOrderService;
import com.leyou.user.client.UserClient;
import com.leyou.user.dto.UserAddressDTO;
import com.leyou.utils.BeanHelper;
import com.leyou.utils.IdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    @Autowired
    private TbOrderService tbOrderService;
    @Autowired
    private TbOrderDetailService tbOrderDetailService;
    @Autowired
    private TbOrderLogisticsService tbOrderLogisticsService;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private ItemClient itemClient;
    @Autowired
    private UserClient userClient;
    public Long createOrder(OrderDTO orderDTO, String userId) {
        long orderId = idWorker.nextId();
    //保存订单表
        TbOrder tbOrder = new TbOrder();
        tbOrder.setPaymentType(orderDTO.getPaymentType());
        //后台复制 orderId
        tbOrder.setOrderId(orderId);
        //user_id
        tbOrder.setUserId(Long.parseLong(userId));
        //status
        tbOrder.setStatus(OrderStatusEnum.INIT.value());
        //b_type 订单业务类型
        tbOrder.setBType(BusinessTypeEnum.MALL.value());
        //total_fee
        //总金额需要后台重新查询计算
        Long totalFee=0L;
        List<CartDTO> carts = orderDTO.getCarts();
        Map<Long, Integer> skuidAndNumMap = carts.stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        //获取所有的skuid
        List<Long> skuIdList= carts.stream().map(CartDTO::getSkuId).collect(Collectors.toList());
        List<SkuDTO> skuList = itemClient.findSkuListByIds(skuIdList);
        ArrayList<TbOrderDetail> orderDetailList = new ArrayList<>();
        for (SkuDTO skuDTO : skuList) {
            Integer num = skuidAndNumMap.get(skuDTO.getId());
            totalFee+=skuDTO.getPrice()*num;
           /* Long price = skuDTO.getPrice();
            for (CartDTO cart : carts) {
                Integer num = cart.getNum();
            }*/
           //保存订单详情表
            TbOrderDetail tbOrderDetail = BeanHelper.copyProperties(skuDTO, TbOrderDetail.class);
            tbOrderDetail.setSkuId(skuDTO.getId());
            tbOrderDetail.setNum(num);
            tbOrderDetail.setOrderId(orderId);
            tbOrderDetail.setImage(StringUtils.substringBefore(skuDTO.getImages(), "/" ));
            orderDetailList.add(tbOrderDetail);
        }
        tbOrder.setTotalFee(totalFee);
        tbOrder.setPostFee(0L);
        tbOrder.setActualFee(tbOrder.getTotalFee());
        //根据传过来的addressId查询UserAddress对象
        UserAddressDTO userAddressById = userClient.findUserAddressById(orderDTO.getAddressId());
        //保存物流信息表
        TbOrderLogistics tbOrderLogistics = BeanHelper.copyProperties(userAddressById, TbOrderLogistics.class);
        tbOrderLogistics.setOrderId(orderId);
        boolean save = tbOrderService.save(tbOrder);
        if (!save){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        boolean b = tbOrderDetailService.saveBatch(orderDetailList);
        if (!b){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        boolean save1 = tbOrderLogisticsService.save(tbOrderLogistics);
        if (!save1){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        //减库存
        itemClient.stockMinus(skuidAndNumMap);
        return orderId;
    }

    public OrderVO findOrderById(Long orderId) {
        String userId = UserHolder.getUserId();
        //查询三个表的数据
        QueryWrapper<TbOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbOrder::getOrderId,orderId);
        queryWrapper.lambda().eq(TbOrder::getUserId,userId);
        queryWrapper.lambda().eq(TbOrder::getStatus,OrderStatusEnum.INIT.value());
        TbOrder tbOrder = tbOrderService.getOne(queryWrapper);
        if (tbOrder==null){
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        OrderVO orderVO = BeanHelper.copyProperties(tbOrder, OrderVO.class);
        TbOrderLogistics tbOrderLogistics = tbOrderLogisticsService.getById(orderId);
        OrderLogisticsVO orderLogisticsVO = BeanHelper.copyProperties(tbOrderLogistics, OrderLogisticsVO.class);
        orderVO.setLogistics(orderLogisticsVO);

        QueryWrapper<TbOrderDetail> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.lambda().eq(TbOrderDetail::getOrderId,orderId);
        List<TbOrderDetail> tbOrderDetails = tbOrderDetailService.list(queryWrapper1);
        List<OrderDetailVO> orderDetailVOS = BeanHelper.copyWithCollection(tbOrderDetails, OrderDetailVO.class);
        orderVO.setDetailList(orderDetailVOS);
        return orderVO;

    }
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PayProperties payProperties;

    @Autowired
    private StringRedisTemplate redisTemplate;
    private String prefix = "ly:order:pay:";
    public String getCodeUrl(Long orderId) {


        try {
            //先从redis中获取，获取不到就应该调用微信的api
            String code_url = redisTemplate.boundValueOps(prefix + orderId).get();
            if(StringUtils.isNotBlank(code_url)){
                return code_url;
            }
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
            Map<String,String> paramMap = new HashMap();
            paramMap.put("appid",payProperties.getAppID());
            paramMap.put("mch_id",payProperties.getMchID());
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paramMap.put("body","乐优商城支付");
            paramMap.put("out_trade_no",orderId.toString());
            paramMap.put("total_fee","1");
            paramMap.put("spbill_create_ip","127.0.0.1");
            // 支付成后 微信会调用一个地址
            paramMap.put("notify_url",payProperties.getNotifyurl());
            paramMap.put("trade_type","NATIVE");

            String paramXml = WXPayUtil.generateSignedXml(paramMap, payProperties.getKey());
            String resultXml = restTemplate.postForObject(url, paramXml, String.class);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);
            code_url = resultMap.get("code_url");

            redisTemplate.boundValueOps(prefix+orderId).set(code_url,2, TimeUnit.HOURS);
            return code_url;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Integer queryOrderPayState(Long orderId) {
        //Spring封装HttpClient 提供一个工具RestTemplate
        try {
            String url = "https://api.mch.weixin.qq.com/pay/orderquery";
            Map<String,String> paramMap = new HashMap();
            paramMap.put("appid",payProperties.getAppID());
            paramMap.put("mch_id",payProperties.getMchID());
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
            paramMap.put("out_trade_no",orderId.toString());
            String paramXml = WXPayUtil.generateSignedXml(paramMap, payProperties.getKey());//把map转成带有签名xml字符串
            String resultXml = restTemplate.postForObject(url, paramXml, String.class); //调用统一下单方法
            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);
            String trade_state = resultMap.get("trade_state");
            if("SUCCESS".equals(trade_state)){
             //修改订单的状态和支付时间
             // update tb_order set status=2 , pay_time= 当前时间  where order_id=？
                TbOrder tbOrder = new TbOrder();
                tbOrder.setOrderId(orderId);
                tbOrder.setStatus(OrderStatusEnum.PAY_UP.value());
                tbOrder.setPayTime(new Date());
               tbOrderService.updateById(tbOrder); //更新支付状态和时间

                return 1;
            }else{
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void cleanOverTimeOrder() {
        //查询所有超时订单的详细数据
        List<TbOrderDetail> orderDetails=tbOrderDetailService.findOverTimeOrderDetail();
        if (orderDetails==null){
            return;
        }
        //更改超时订单为5 超时取消
        tbOrderService.updateOverTimeOrderStutas();
        //加库存
        Map<Long, Integer> skuIdAndNumMap = orderDetails.stream().collect(Collectors.toMap(TbOrderDetail::getSkuId, TbOrderDetail::getNum));

        itemClient.stockPlus(skuIdAndNumMap);
        System.out.println(new Date());
    }
}
