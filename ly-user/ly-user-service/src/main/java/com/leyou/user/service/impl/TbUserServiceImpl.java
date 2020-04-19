package com.leyou.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.enums.ExceptionEnum;
import com.leyou.exception.LyException;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.TbUser;
import com.leyou.user.mapper.TbUserMapper;
import com.leyou.user.service.TbUserService;
import com.leyou.utils.BeanHelper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.leyou.constants.RocketMQConstants.TAGS.VERIFY_CODE_TAGS;
import static com.leyou.constants.RocketMQConstants.TOPIC.SMS_TOPIC_NAME;
import static com.leyou.constants.SmsConstants.*;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author SYL
 * @since 2020-02-11
 */
@Service
public class TbUserServiceImpl extends ServiceImpl<TbUserMapper, TbUser> implements TbUserService {

    @Override
    public Boolean checkUserData(String data, Integer type) {
        //查询数据库中是否有输入的用户名和手机号
        QueryWrapper<TbUser> queryWrapper = new QueryWrapper<>();
        if (type==1){
            //等于1表示用户名
            queryWrapper.lambda().eq(TbUser::getUsername,data);
        }else if (type==2){
            //等于2表示手机号
            queryWrapper.lambda().eq(TbUser::getPhone,data);
        }else {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }

        int count = this.count(queryWrapper);
        //System.out.println(count);
        return count==0?true:false;

    }
    @Autowired(required = false)
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Override
    public void sendCode(String phone) {
        //发送验证码消息到MQ
        Map map=new HashMap();
        map.put(SMS_PARAM_KEY_PHONE,phone);
        map.put(SMS_PARAM_KEY_SIGN_NAME, "金陵十三衩");
        map.put(SMS_PARAM_KEY_TEMPLATE_CODE, "SMS_178771219");
        String numeric = RandomStringUtils.randomNumeric(4);
        map.put(SMS_PARAM_KEY_TEMPLATE_PARAM, "{\"code\":\""+numeric+"\"}");
        rocketMQTemplate.convertAndSend(SMS_TOPIC_NAME+":"+VERIFY_CODE_TAGS,map);
        //将验证码存入到redis
        redisTemplate.boundValueOps("ly:sms:"+phone).set(numeric,1, TimeUnit.MINUTES);
       // redisTemplate.boundValueOps("ly:sms:"+phone).set(numeric);
    }
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Override
    public void register(TbUser tbUser, String code) {
        //检验验证码是否正确
        String code_redis = redisTemplate.boundValueOps("ly:sms:" + tbUser.getPhone()).get();
        //查不到的话提示超时
        if (StringUtils.isEmpty(code_redis)){
            throw new LyException(ExceptionEnum.TIME_OUT_CODE);
        }
        //不相同验证码错误
        if (!StringUtils.equals(code, code_redis)){
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        //密码加密
        String password = tbUser.getPassword();
        //System.out.println(password);
        String encodePassword = bCryptPasswordEncoder.encode(password);
        //System.out.println(encodePassword);
        tbUser.setPassword(encodePassword);

        //存入数据库
        boolean save = this.save(tbUser);
        if (!save){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //从redis中删除验证码
        redisTemplate.delete("ly:sms:"+tbUser.getPhone());
    }

    @Override
    public UserDTO queryUsernameAndPassword(String username, String password) {
        QueryWrapper<TbUser> queryWrapper = new QueryWrapper<>();
        //先查用户名
        queryWrapper.lambda().eq(TbUser::getUsername,username);
        TbUser tbUser = this.getOne(queryWrapper);
        if (tbUser==null){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        //比较密码
        String passwordh = tbUser.getPassword();
        boolean result = bCryptPasswordEncoder.matches(password, passwordh);
        if (!result){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        UserDTO userDTO = BeanHelper.copyProperties(tbUser, UserDTO.class);
        return userDTO;
    }
}
