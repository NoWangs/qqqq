package com.leyou.gateway.config;


import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements  InitializingBean {

    private String pubKeyPath;

    private PublicKey publicKey;   //根据公钥的文件路径生成

    //确定pubKeyPath和priKeyPath赋值成功后才能执行下面这个方法
    @Override
    public void afterPropertiesSet() throws Exception {
        publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }
    private UserTokenProperties user  = new UserTokenProperties();

    @Data
    public class UserTokenProperties {
        private String cookieName;
    }

}