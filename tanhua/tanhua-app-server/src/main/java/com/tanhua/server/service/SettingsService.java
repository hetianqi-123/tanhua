package com.tanhua.server.service;

import api.BlackListApi;
import api.QuestionApi;
import api.SettingApi;
import api.UserApi;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.server.interceptor.UserHolder;
import domain.*;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vo.PageResult;

import java.util.Map;

@Service
public class SettingsService {

    @DubboReference
    //通用设置
    private SettingApi settingApi;
    @DubboReference
    //获取陌生人
    private QuestionApi questionApi;
    @DubboReference
    //调用黑名单的实现类
    private BlackListApi blackListApi;
    //注入redis对象
    @Autowired
    private RedisTemplate<String ,String > redisTemplate;

    @DubboReference
    private UserApi userApi;


    /**
     * 用户通用设置 - 读取
     * @return
     */
    public SettingsVo settings() {
        //把获取的两个数据库的值保存到SettingsVo中
        SettingsVo settingsVo =new SettingsVo();
        //获取当前用户的id
        Long userId = UserHolder.getUserId();
        //保存到SettingsVo中
        settingsVo.setId(userId);
        //获取到当前用户的手机号
        String mobile = UserHolder.getMobile();
        settingsVo.setPhone(mobile);
        //获取陌生人问题
        Question question = questionApi.findByUserId(userId);
        //如果没有消息，显示默认值
        String txt= question==null?"你喜欢java吗？":question.getTxt();
        settingsVo.setStrangerQuestion(txt);
        //根据id查询通用设置Settings
        Settings settings= settingApi.setting(userId);
        if(settings!=null){
            settingsVo.setGonggaoNotification(settings.getGonggaoNotification());
            settingsVo.setLikeNotification(settings.getLikeNotification());
            settingsVo.setPinglunNotification(settings.getPinglunNotification());
        }
        System.out.println(settings);
        return settingsVo;
    }

    /**
     * 设置陌生人问题
     * @param content 前端传入的问题
     */
    public void questionSettings(String content) {
        System.out.println("前端传来的消息"+content);
        //获取到当前用户的id
        Long userId = UserHolder.getUserId();
        //判断当前用户是否有陌生人问题
        //查询陌生人问题
        Question   question = questionApi.findByUserId(userId);
        //如果当前用户没有陌生人问题,进行保存
        if(question==null){
           //重新new Question对象，因为是个null值，赋值就报错
             question =new Question();
            //当前用户userid和传来的消息存到Question中
            question.setUserId(userId);
            //把传来的消息保存到封装类中
            question.setTxt(content);
            questionApi.saveSettings(question);
        }else {
            //把传来的消息保存到封装类中
            question.setTxt(content);
            //如果当前用户有陌生人问题，进行更新
            questionApi.updateSettings(question);
        }

    }

    /**
     * 通知设置 - 保存
     * @param map
     */
    public void tzsettings(Map map) {
        //获取到当前用户uid
        Long userId = UserHolder.getUserId();
        //获取到当前设置
        boolean likeNotification = (boolean)map.get("likeNotification");
        boolean pinglunNotification = (boolean)map.get("pinglunNotification");
        boolean gonggaoNotification = (boolean)map.get("gonggaoNotification");
        //2、根据用户id，查询用户的通知设置
        Settings setting = settingApi.setting(userId);
        //判断用户有没有设置
        if(setting==null){
            setting=new Settings();
            //保存到Settings封装类中
            setting.setUserId(userId);
            setting.setGonggaoNotification(gonggaoNotification);
            setting.setPinglunNotification(pinglunNotification);
            setting.setLikeNotification(likeNotification);
            //进行保存
            settingApi.save(setting);
        }else {
            //保存到Settings封装类中
            setting.setUserId(userId);
            setting.setGonggaoNotification(gonggaoNotification);
            setting.setPinglunNotification(pinglunNotification);
            setting.setLikeNotification(likeNotification);
            //进行修改
            settingApi.update(setting);
        }





    }

    /**
     * 黑名单分页查询
     * @param page  当前页
     * @param pagesize 每页显示的条数
     * @return
     */
    public PageResult blacklist(int page, int pagesize) {
        //1、获取当前用户的id
        Long userId = UserHolder.getUserId();
        //2、调用API查询用户的黑名单分页列表  Ipage对象
      IPage<UserInfo> iPage =blackListApi.findByUserId(userId,page,pagesize);
        //3、对象转化，将查询的Ipage对象的内容封装到PageResult中
        PageResult pr = new PageResult(page,pagesize,(int)iPage.getTotal(),iPage.getRecords());
        //4、返回
        return pr;
    }

    /**
     * 移除黑名单
     * @param blackUserId
     */
    public void deletefanye(Long blackUserId) {
        //获取当前用户的id
        Long userId = UserHolder.getUserId();
        //调用删除的方法
        blackListApi.deletefanye(blackUserId,userId);
    }

    /**
     * 校验验证码
     * @param mobile 手机号
     * @param code  验证码
     * @return
     */
    public verification checkcode(String mobile, String code) {
        //从redis中获取验证码
        String redisCode = redisTemplate.opsForValue().get("CHECK_CODE_"+mobile);
        verification verification=new verification();
        //2、对验证码进行校验（验证码是否存在，是否和输入的验证码一致）
        if(StringUtils.isEmpty(redisCode) || !redisCode.equals(code)) {
            //验证码无效
//            throw new RuntimeException("验证码错误");
            verification.setVerification(false);
            return verification;
        }else {
            //3、删除redis中的验证码
            redisTemplate.delete("CHECK_CODE_"+mobile);
        }
        verification.setVerification(true);

        return verification;


    }


    /**
     * 判断新手机号是否存在
     * @param phone
     */
    public void isphone(String phone) {
        //查询手机号
        User byMobile = userApi.findByMobile(phone);
        //获取到当前用户id
        Long userId = UserHolder.getUserId();

        //判断user是否为null
        if(byMobile!=null){
            //说明新手机号已经被注册,抛出异常
            throw new RuntimeException("手机号已经被注册");
        }else {
        //将旧手机号替换为新手机号
            User user=new User();
            user.setId(userId);
            user.setMobile(phone);
            userApi.update(user);
        }
    }
}
