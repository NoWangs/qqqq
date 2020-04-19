package com.leyou.task.job;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.joda.time.DateTime;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.leyou.constants.RocketMQConstants.TAGS.ORDER_OVERTIME_TAGS;
import static com.leyou.constants.RocketMQConstants.TAGS.SECKILL_BEGIN_TAGS;
import static com.leyou.constants.RocketMQConstants.TOPIC.ORDER_TOPIC_NAME;
import static com.leyou.constants.RocketMQConstants.TOPIC.SECKILL_TOPIC_NAME;

@Component
public class CleanOrderTask {
    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Scheduled(cron = "* * * * 11 ?")
    public void sendCleanOrderTask(){
        RLock lock = redissonClient.getLock("overtimetask");
        boolean b = lock.tryLock();
        if (b){
            try{
            rocketMQTemplate.convertAndSend(ORDER_TOPIC_NAME+":"+ORDER_OVERTIME_TAGS,"开始清理");
            System.out.println("消息发送成功");
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                lock.unlock();
                System.out.println("释放锁成功");
            }

        }


    }

}
