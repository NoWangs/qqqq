package com.leyou.gateway.filter;

import com.leyou.auth.entity.Payload;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.gateway.config.AuthProperties;
import com.leyou.gateway.config.JwtProperties;
import com.leyou.utils.CookieUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

@Component
public class AuthFilter extends ZuulFilter {
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private AuthProperties authProperties;
    //什么类型的过滤器
    @Override
    public String filterType() {
        return PRE_TYPE;


    }
    //过滤器执行顺序  越小越早
    @Override
    public int filterOrder() {
        return FORM_BODY_WRAPPER_FILTER_ORDER-1;
    }
    //是否执行过滤方法
    @Override
    public boolean shouldFilter() {
        //需要把不需要登录的请求放行
        //获取白名单
        List<String> allowPaths = authProperties.getAllowPaths();
        //当前请求在白名单中则放行
        //获取当前请求uri
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String uri = request.getRequestURI();
        //遍历白名单
        for (String allowPath : allowPaths) {

            //当前路径以白名单开始则放行
            if (uri.startsWith(allowPath)){
                return false;
            }
        }
        return true;
    }
    //过滤方法
    @Override
    public Object run() throws ZuulException {
        //看token能否解密,解密不成功不许同行
        //获取token
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String token = CookieUtils.getCookieValue(request, jwtProperties.getUser().getCookieName());
        //解密token
        try {
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey(), UserInfo.class);
            UserInfo userInfo = payload.getUserInfo();
            //获取userid放到为附中
            Long userId = userInfo.getId();
            ctx.addZuulRequestHeader("USER_ID", userId.toString());
            //TODO
        }catch (Exception e){
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(HttpStatus.FORBIDDEN.value());
            e.printStackTrace();
        }
        return null;
    }
}
