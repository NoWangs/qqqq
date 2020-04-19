package com.leyou.auth.service;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.Payload;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import com.leyou.enums.ExceptionEnum;
import com.leyou.exception.LyException;
import com.leyou.user.client.UserClient;
import com.leyou.user.dto.UserDTO;
import com.leyou.utils.BeanHelper;
import com.leyou.utils.CookieUtils;
import org.bouncycastle.cms.PasswordRecipientId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.PrivateKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    @Autowired
    private UserClient userClient;
    @Autowired
    private JwtProperties jwtProperties;
    public void login(String username, String password, HttpServletResponse response) throws Exception{
        //调用用户微服务查询用户信息
        UserDTO userDTO = userClient.queryUsernameAndPassword(username, password);
        //转成userinfo
        UserInfo userInfo = BeanHelper.copyProperties(userDTO, UserInfo.class);
        //加密
        //获取私钥
        //PrivateKey privateKey = RsaUtils.getPrivateKey("E:/xiangmu/leyou118/id_rsa");  已在配置类中声明
        //生成token
        generateTokenAndSetCookie(response, userInfo);
    }

    private void generateTokenAndSetCookie(HttpServletResponse response, UserInfo userInfo) {
        String token = JwtUtils.generateTokenExpireInMinutes(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getUser().getExpire());
        //写入cookie
        CookieUtils.newCookieBuilder().name(jwtProperties.getUser().getCookieName())
                                        .value(token)
                                        .domain(jwtProperties.getUser().getCookieDomain())
                                        .httpOnly(true)
                                        .maxAge(60*60*24*7)
                                        .response(response).build();
    }

    public UserInfo verify(HttpServletRequest request, HttpServletResponse response) {
       try { //获取token
           String token = CookieUtils.getCookieValue(request, jwtProperties.getUser().getCookieName());
           //解析token  返回载荷
           Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey(), UserInfo.class);
           //判断payloadid是否在redis中
           if (redisTemplate.hasKey(payload.getId())){
               return null;
           }
           //获取超时时间
           Date expirationtime = payload.getExpiration();
           //从载荷中获取userInfo
           UserInfo userInfo = payload.getUserInfo();
           if (new DateTime(expirationtime).minusMinutes(jwtProperties.getUser().getMinRefreshInterval()).isBeforeNow()){
               generateTokenAndSetCookie(response, userInfo);
           }
           return userInfo;
       }catch (Exception e) {
           return null;
       }

    }
@Autowired
private StringRedisTemplate redisTemplate;
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        //获取token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getUser().getCookieName());
        //解析token
        Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey(), UserInfo.class);
        //存入redis黑名单  载荷id存
        String payloadId = payload.getId();
        //超时时间减现在  剩余多少时间超时
        Long l=payload.getExpiration().getTime()-new Date().getTime();
        redisTemplate.boundValueOps(payloadId).set("",l, TimeUnit.MINUTES);
        //删除cookie
        CookieUtils.deleteCookie(jwtProperties.getUser().getCookieName(), jwtProperties.getUser().getCookieDomain(), response);

    }
}
