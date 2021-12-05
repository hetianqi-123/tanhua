package com.tanhua.server.interceptor;

import domain.User;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import utils.JwtUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 配置token拦截器
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
       //获取请求头，获取token
        String token = request.getHeader("Authorization");
        //拦截token,判断token是否有效
//        boolean b = JwtUtils.verifyToken(token);
//        //如果失效，拦截，返回401
//        if(!b){
//            response.setStatus(401);
//            return false;
//        }
        //解析token,获取id和手机号，构造User对象，存入ThreadLocal中
        Claims claims = JwtUtils.getClaims(token);
        Integer id = (Integer) claims.get("id");
        String mobile = (String)claims.get("mobile");
        User user=new User();
        user.setId(Long.valueOf(id));
        user.setMobile(mobile);
        //把User存入ThreadLocal中
        UserHolder.set(user);
        //如果有效，放行
        return true;
    }



    //执行完所有方法的时候，移除UserHolder线程中的User对象，避免累积过多影响性能
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.remove();
    }
}
