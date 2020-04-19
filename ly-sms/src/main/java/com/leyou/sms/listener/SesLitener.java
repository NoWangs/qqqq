package com.leyou.sms.listener;

import com.leyou.sms.utils.SendSmsUtil;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.leyou.constants.RocketMQConstants.CONSUMER.SMS_VERIFY_CODE_CONSUMER;
import static com.leyou.constants.RocketMQConstants.TAGS.VERIFY_CODE_TAGS;
import static com.leyou.constants.RocketMQConstants.TOPIC.SMS_TOPIC_NAME;
import static com.leyou.sms.constants.SmsConstants.*;

@Component
@RocketMQMessageListener(consumerGroup = SMS_VERIFY_CODE_CONSUMER,topic = SMS_TOPIC_NAME,selectorExpression =VERIFY_CODE_TAGS )
public class SesLitener implements RocketMQListener<Map> {
    @Autowired
    private SendSmsUtil sendSmsUtil;
    public void onMessage(Map map){
        String PhoneNumbers = map.get(SMS_PARAM_KEY_PHONE).toString();//电话号码
        String SignName = map.get(SMS_PARAM_KEY_SIGN_NAME).toString();//签名名称
        String TemplateCode = map.get(SMS_PARAM_KEY_TEMPLATE_CODE).toString();//模版code
        String TemplateParam = map.get(SMS_PARAM_KEY_TEMPLATE_PARAM).toString();//模版内容

        sendSmsUtil.sendSms(PhoneNumbers, SignName, TemplateCode, TemplateParam);
        System.out.println("短信发送成功");
    }

}
