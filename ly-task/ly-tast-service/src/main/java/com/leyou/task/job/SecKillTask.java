package com.leyou.task.job;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.joda.time.DateTime;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.leyou.constants.RocketMQConstants.TAGS.SECKILL_BEGIN_TAGS;
import static com.leyou.constants.RocketMQConstants.TAGS.SECKILL_ORDER_TAGS;
import static com.leyou.constants.RocketMQConstants.TOPIC.SECKILL_TOPIC_NAME;

@Component
public class SecKillTask {
    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @Scheduled(cron = "0/30 * * * * ?")
    public void createSeckillTask(){
        RLock lock = redissonClient.getLock("seckill");
        boolean b = lock.tryLock();
        if (b){
            try {
                String date = DateTime.now().toString("yyyy-MM-dd");

                rocketMQTemplate.convertAndSend(SECKILL_TOPIC_NAME+":"+SECKILL_ORDER_TAGS ,date);
                System.out.println("生成秒杀页面的消息已发出");
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }


    }
}
