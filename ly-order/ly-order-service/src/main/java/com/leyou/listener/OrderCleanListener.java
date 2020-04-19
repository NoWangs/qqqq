package com.leyou.listener;

import com.leyou.service.impl.OrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.leyou.constants.RocketMQConstants.CONSUMER.ORDER_OVERTIME_CONSUMER;
import static com.leyou.constants.RocketMQConstants.TAGS.ORDER_OVERTIME_TAGS;
import static com.leyou.constants.RocketMQConstants.TOPIC.ORDER_TOPIC_NAME;

@Component
@RocketMQMessageListener(consumerGroup = ORDER_OVERTIME_CONSUMER,topic =ORDER_TOPIC_NAME ,selectorExpression = ORDER_OVERTIME_TAGS)
public class OrderCleanListener implements RocketMQListener<String> {
    @Autowired
    private OrderService orderService;
    @Override
    public void onMessage(String s) {
        orderService.cleanOverTimeOrder();
    }
}
