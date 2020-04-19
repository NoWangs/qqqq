package com.leyou.test;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.leyou.constants.RocketMQConstants.TAGS.VERIFY_CODE_TAGS;
import static com.leyou.constants.RocketMQConstants.TOPIC.SMS_TOPIC_NAME;
import static com.leyou.sms.constants.SmsConstants.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsTest {
    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;
    @Test
    public void smsTest(){
        Map map=new HashMap();
        map.put(SMS_PARAM_KEY_PHONE, "13653459738");
        map.put(SMS_PARAM_KEY_SIGN_NAME, "金陵十三衩");
        map.put(SMS_PARAM_KEY_TEMPLATE_CODE, "SMS_178771219");
        String numeric = RandomStringUtils.randomNumeric(4);
        map.put(SMS_PARAM_KEY_TEMPLATE_PARAM, "{\"code\":\""+numeric+"\"}");
        rocketMQTemplate.convertAndSend(SMS_TOPIC_NAME+":"+VERIFY_CODE_TAGS,map);
    }





}
