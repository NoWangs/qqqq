package com.leyou.listener;


import com.leyou.service.PageService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.leyou.constants.RocketMQConstants.CONSUMER.ITEM_PAGE_DOWN_CONSUMER;
import static com.leyou.constants.RocketMQConstants.CONSUMER.ITEM_SEARCH_DOWN;
import static com.leyou.constants.RocketMQConstants.TAGS.ITEM_DOWN_TAGS;
import static com.leyou.constants.RocketMQConstants.TOPIC.ITEM_TOPIC_NAME;

@Component
@RocketMQMessageListener(consumerGroup=ITEM_PAGE_DOWN_CONSUMER,topic = ITEM_TOPIC_NAME,selectorExpression=ITEM_DOWN_TAGS)
public class ItemDownListener implements RocketMQListener<Long> {
    @Autowired
    private PageService pageService;
    @Override
    public void onMessage(Long spuId) {
        pageService.removeHtml(spuId);

    }
}
