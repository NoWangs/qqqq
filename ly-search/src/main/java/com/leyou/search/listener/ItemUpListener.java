package com.leyou.search.listener;

import com.leyou.search.service.SearchService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.leyou.constants.RocketMQConstants.CONSUMER.ITEM_SEARCH_UP;
import static com.leyou.constants.RocketMQConstants.TAGS.ITEM_UP_TAGS;
import static com.leyou.constants.RocketMQConstants.TOPIC.ITEM_TOPIC_NAME;

@Component
@RocketMQMessageListener(consumerGroup=ITEM_SEARCH_UP,topic=ITEM_TOPIC_NAME,selectorExpression=ITEM_UP_TAGS)
public class ItemUpListener implements RocketMQListener<Long> {
    @Autowired
    private SearchService searchService;
    @Override
    public void onMessage(Long spuId) {
        searchService.createGoods(spuId);
    }
}
