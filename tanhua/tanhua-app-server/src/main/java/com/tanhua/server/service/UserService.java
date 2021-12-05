package com.tanhua.server.service;


import api.UserApi;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.autoconfig.template.SmsTemplate;
import com.tanhua.server.exception.BusinessException;
import domain.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import utils.Constants;
import utils.JwtUtils;
import vo.ErrorResult;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    //注入发送验证码的类
    @Autowired
    private SmsTemplate smsTemplate;
    //注入redis对象，用来操作验证码
    @Autowired
    private RedisTemplate<String ,String> redisTemplate;

    //注入环信工具类对象
    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @DubboReference
    private UserApi userApi;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private MqMessageService mqMessageService;

    @Autowired
    private UserFreezeService userFreezeService;
    /**
     * 发送验证码的功能
     * @param phone 手机号
     */
    public void sendMsg(String phone) {
        //根据手机号查询用户，如果用户存在，判断是否被冻结
        User user = userApi.findByMobile(phone);
        if(user != null) {
            //状态为1表示禁止登陆
            userFreezeService.checkUserStatus("1",user.getId());
        }
        //1.生成个6位随机数
        //String code = RandomStringUtils.random(6);
        String code="123456";
        //2.调用短信发送的方法，把手机号和随机数传值过去
      // smsTemplate.sendSms(phone,code);
        //3.把随机数保存到redis里面，并且设置验证码的时效性为5分钟
        redisTemplate.opsForValue().set("CHECK_CODE_"+phone,code, Duration.ofMinutes(20));
    }


    /**
     * 验证登录
     * @param phone
     * @param code
     */
    public Map loginVerification(String phone, String code) {
        System.out.println(userApi);
        //1、从redis中获取下发的验证码
        String redisCode = redisTemplate.opsForValue().get("CHECK_CODE_"+phone);
        System.out.println(redisCode);
        //2、对验证码进行校验（验证码是否存在，是否和输入的验证码一致）
        if(StringUtils.isEmpty(redisCode) || !redisCode.equals(code)) {
            //验证码无效
            throw new BusinessException(ErrorResult.loginError());
        }
        //3、删除redis中的验证码
        redisTemplate.delete("CHECK_CODE_" + phone);
        //4、通过手机号码查询用户
        User user = userApi.findByMobile(phone);
        boolean isNew = false;
        //5、如果用户不存在，创建用户保存到数据库中
        String type="0101";
        if(user == null) {
            type="0102";
            user = new User();
            user.setMobile(phone);
            user.setPassword(DigestUtils.md5Hex("123456"));
            //保存到数据库中
            Long userId = userApi.save(user);
            user.setId(userId);
            isNew = true;
            //将用户注册到环信中,以hx加用户id作为环信的用户id
            String hxUser="hx"+user.getId();
            //调用环信的注册方法,返回布尔类型
            Boolean create = huanXinTemplate.createUser(hxUser, Constants.HX_USER_PREFIX);
            //如果注册成功,把输入保存到数据库中
            if(create){
                user.setHxUser(hxUser);
                //密码默认123456
                user.setHxPassword(Constants.INIT_PASSWORD);
                //进行更新数据库
                userApi.update(user);
            }

        }


        //消息队列发送消息
        mqMessageService.sendLogMessage(user.getId(),type,"user",null);

        //6、通过JWT生成token(存入id和手机号码)
        Map tokenMap = new HashMap();
        tokenMap.put("id",user.getId());
        tokenMap.put("mobile",phone);
        String token = JwtUtils.getToken(tokenMap);
        //7、构造返回值
        Map retMap = new HashMap();
        retMap.put("token",token);
        retMap.put("isNew",isNew);
        return retMap;
    }
}
