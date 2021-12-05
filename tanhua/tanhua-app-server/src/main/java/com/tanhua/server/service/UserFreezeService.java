package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.server.exception.BusinessException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import utils.Constants;
import vo.ErrorResult;

import java.util.Map;

@Service
public class UserFreezeService {
    @Autowired
    private RedisTemplate<String,String>redisTemplate;

    /**
     * 冻结范围，1为冻结登录，2为冻结发言，3为冻结发布动态
     * 判断用户是否被冻结，已被冻结，抛出异常
     *  参数：冻结范围（1，2，3），用户id
     *
     *  检测登录：
     *     checkUserStatus（“1”，106）
     */
    public void checkUserStatus(String state,Long userId) {
        //拼接key
        String key = Constants.USER_FREEZE + userId;
        //获取到value
        String value = redisTemplate.opsForValue().get(key);
        //查询value是否有值
        //如果没有,直接就登录了
        //如果有的话,判断是不是登录冻结,登录冻结为1
        if(!StringUtils.isEmpty(value)){
            //将REDIS中存储的JSON串转换为MAP
            Map map = JSON.parseObject(value, Map.class);
            //从map中获取冻结类型
            String freezingRange = (String) map.get("freezingRange");
            //判断从redis中获取的冻结类型是否和传递过来的一值
            if(state.equals(freezingRange)){
                //如果一致,抛出异常
                throw new BusinessException(ErrorResult.builder().errMessage("用户被冻结").build());
            }

        }

    }
}
