package com.leyou.listener;

import com.leyou.service.PageService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.leyou.constants.RocketMQConstants.CONSUMER.SECKILL_BEGIN_CONSUMER;
import static com.leyou.constants.RocketMQConstants.TAGS.SECKILL_BEGIN_TAGS;
import static com.leyou.constants.RocketMQConstants.TAGS.SECKILL_ORDER_TAGS;
import static com.leyou.constants.RocketMQConstants.TOPIC.SECKILL_TOPIC_NAME;

@Component
@RocketMQMessageListener(topic = SECKILL_TOPIC_NAME,
        selectorExpression = SECKILL_ORDER_TAGS,
        consumerGroup = SECKILL_BEGIN_CONSUMER)
public class createSeckillTaskListener implements RocketMQListener<String> {
    @Autowired
    private PageService pageService;
    @Override
    public void onMessage(String date) {
        pageService.createSeckillHtml(date);
    }
}
