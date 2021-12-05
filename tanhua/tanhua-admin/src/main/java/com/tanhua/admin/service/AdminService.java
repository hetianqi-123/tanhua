package com.tanhua.admin.service;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.admin.exception.BusinessException;
import com.tanhua.admin.interceptor.AdminHolder;
import com.tanhua.admin.mapper.AdminMapper;
import domain.Admin;
import mongo.AdminVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import utils.Constants;
import utils.JwtUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 用户登录
     * @param map
     * @return
     */
    public Map login(Map map) {
        //1、获取请求参数
        String username = (String )map.get("username");
        String password = (String )map.get("password");
        String verificationCode = (String )map.get("verificationCode");
        String uuid = (String )map.get("uuid");
        //2、检验验证码是否正确
        String key = Constants.CAP_CODE + uuid;
        //获取到value
        String value = redisTemplate.opsForValue().get(key);
        //如果value为空,或者前端传来的验证码和redis不同
        if(StringUtils.isEmpty(value) || !verificationCode.equals(value)) {
            throw  new BusinessException("验证码错误");
        }
        //删除验证码
        redisTemplate.delete(key);
        //3、根据用户名查询管理员对象 Admin
        QueryWrapper<Admin> qw = new QueryWrapper<Admin>().eq("username",username);
        Admin admin = adminMapper.selectOne(qw);
        //4、判断admin对象是否存在，密码是否一致
        //先把密码进行加密
        password = SecureUtil.md5(password);
        if(admin == null || !password.equals(admin.getPassword())) {
            throw  new BusinessException("用户名或者密码错误");
        }
        //5、保存到hashmap中登陆成功生成token
        Map tokenMap = new HashMap();
        tokenMap.put("id",admin.getId());
        tokenMap.put("username",admin.getUsername());

        String token = JwtUtils.getToken(tokenMap);
        //6、构造返回值
        Map retMap = new HashMap();
        retMap.put("token",token);
        //返回个token
        return retMap;
    }

    /**
     * 登录之后获取用户信息
     * @return
     */
    public AdminVo profile() {
        Long id = AdminHolder.getUserId();
        Admin admin = adminMapper.selectById(id);
        return AdminVo.init(admin);
    }
}
