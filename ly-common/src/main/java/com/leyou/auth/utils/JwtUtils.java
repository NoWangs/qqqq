package com.leyou.auth.utils;

import com.leyou.auth.entity.Payload;
import com.leyou.auth.entity.UserInfo;
import com.leyou.utils.JsonUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.joda.time.DateTime;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.UUID;


public class JwtUtils {

    private static final String JWT_PAYLOAD_USER_KEY = "user";

    /**
     * 私钥加密token
     *
     * @param userInfo   载荷中的数据
     * @param privateKey 私钥
     * @param expire     过期时间，单位分钟
     * @return JWT
     */
    public static String generateTokenExpireInMinutes(Object userInfo, PrivateKey privateKey, int expire) {
        return Jwts.builder()
                .claim(JWT_PAYLOAD_USER_KEY, JsonUtils.toString(userInfo))
                .setId(createJTI())
                .setExpiration(DateTime.now().plusMinutes(expire).toDate())
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * 私钥加密token
     *
     * @param userInfo   载荷中的数据
     * @param privateKey 私钥
     * @param expire     过期时间，单位秒
     * @return JWT
     */
    public static String generateTokenExpireInSeconds(Object userInfo, PrivateKey privateKey, int expire) {
        return Jwts.builder()
                .claim(JWT_PAYLOAD_USER_KEY, JsonUtils.toString(userInfo))
                .setId(createJTI())
                .setExpiration(DateTime.now().plusSeconds(expire).toDate())
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * 公钥解析token
     *
     * @param token     用户请求中的token
     * @param publicKey 公钥
     * @return Jws<Claims>
     */
    private static Jws<Claims> parserToken(String token, PublicKey publicKey) {
        return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);
    }

    private static String createJTI() {
        return new String(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes()));
    }

    /**
     * 获取token中的用户信息
     *
     * @param token     用户请求中的令牌
     * @param publicKey 公钥
     * @return 用户信息
     */
    public static <T> Payload<T> getInfoFromToken(String token, PublicKey publicKey, Class<T> userType) {
        Jws<Claims> claimsJws = parserToken(token, publicKey);
        Claims body = claimsJws.getBody();
        Payload<T> claims = new Payload<>();
        claims.setId(body.getId());
        claims.setUserInfo(JsonUtils.toBean(body.get(JWT_PAYLOAD_USER_KEY).toString(), userType));
        claims.setExpiration(body.getExpiration());
        return claims;
    }

    /**
     * 获取token中的载荷信息
     *
     * @param token     用户请求中的令牌
     * @param publicKey 公钥
     * @return 用户信息
     */
    public static <T> Payload<T> getInfoFromToken(String token, PublicKey publicKey) {
        Jws<Claims> claimsJws = parserToken(token, publicKey);
        Claims body = claimsJws.getBody();
        Payload<T> claims = new Payload<>();
        claims.setId(body.getId());
        claims.setExpiration(body.getExpiration());
        return claims;
    }


    //测试一下生成token  解密

    public static void main(String[] args)throws Exception {
        //拿到私钥
        /*PrivateKey privateKey = RsaUtils.getPrivateKey("E:\\xiangmu\\leyou118\\id_rsa");
        //生层token
        UserInfo userInfo = new UserInfo(10L,"lisi","vip");
        // userinfo  私钥  超时时间
        String token = JwtUtils.generateTokenExpireInMinutes(userInfo, privateKey, 30);
        System.out.println(token);*/
        //解密
        String token="eyJhbGciOiJSUzI1NiJ9.eyJ1c2VyIjoie1wiaWRcIjoxMCxcInVzZXJuYW1lXCI6XCJsaXNpXCIsXCJyb2xlXCI6XCJ2aXBcIn0iLCJqdGkiOiJZVEUzWXpGaU9UQXRNamswWlMwMFlqQXpMV0kxTUdRdE9XTTJOR0ZqTVdVek16SXgiLCJleHAiOjE1ODI3MDgxNzh9.1oONUZIlo-fi245Qc9UlV3WO9uKWe3RZMhnOEPQoqwoaXLFAqvJ_OfkRn0UGjK2gwaeBffxEwhNybJLOjB4fqVpnpVpZZctggEsLnGMep4I6ir6FdnL5uyX4NisJKxEx8amGtDfvB49jJ8ow6G8QDuz9dNFNZwIHDAeQUGtSAayByOLR6TTIIEbuJ4o0uy_ze498JEUMuNxBkKI6Ct65fEDL8zrxG0srX1ZOD_65U183bVi3RvtqDThK4l3L2wkUtqJ1NB25puLG76npA8seHfvPJVx-sSCR_TFbL0bTm1Oh5ACXbQbwk2yH8rNem5PnuDgPMOgRdG3_OOFYEZkdwg";
        //拿到公钥
        PublicKey publicKey = RsaUtils.getPublicKey("E:\\xiangmu\\leyou118\\id_rsa.pub");
        //解密  token  公钥  类型
        Payload<UserInfo> paylosd = JwtUtils.getInfoFromToken(token, publicKey, UserInfo.class);
        System.out.println(paylosd.toString());


    }


}
