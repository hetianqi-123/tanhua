package com.tanhua.admin.service;

import api.MovementApi;
import api.UserInfoApi;
import api.VideoApi;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import domain.UserInfo;
import mongo.Movement;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import utils.Constants;
import vo.MovementsVo;
import vo.PageResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ManagerService {

    @DubboReference
    private UserInfoApi userInfoApi;
    @DubboReference
    private VideoApi videoApi;
    @DubboReference
    private MovementApi movementApi;
    @Autowired
    private RedisTemplate<String,String>redisTemplate;

    /**
     * 查询用户列表
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult findAllUsers(Integer page, Integer pagesize) {
      IPage<UserInfo> iPage= userInfoApi.findAll(page,pagesize);
        List<UserInfo> records = iPage.getRecords();
        for (UserInfo userInfo : records) {
            //查询redis中的是否存在冻结用户
            if(redisTemplate.hasKey(Constants.USER_FREEZE + userInfo.getId())){
                userInfo.setUserStatus("2");
            }
        }

      return  new PageResult(page, pagesize, (int) iPage.getTotal(), iPage.getRecords());
    }

    /**
     * 单个用户的详细信息
     * @param userId
     * @return
     */
    public UserInfo findById(Long userId) {
        UserInfo info = userInfoApi.findById(userId);
        //查询redis中的是否存在冻结用户
        if(redisTemplate.hasKey(Constants.USER_FREEZE + userId)){
        info.setUserStatus("2");
        }
        return info;
    }


    /**
     * 查看视频列表
     * @param page
     * @param pagesize
     * @param uid
     * @return
     */
    public PageResult findAllVideos(Integer page, Integer pagesize, Long uid) {
        //分页查询当前用户发的视频

       return videoApi.findByUserId(page,pagesize,uid);
    }

    /**
     * 查询动态列表
     * @param page
     * @param pagesize
     * @param uid
     * @param state
     * @return
     */
    public PageResult findAllMovements(Integer page, Integer pagesize, Long uid, Integer state) {
        //把id和状态,分页都传递过去进行查询
        PageResult pageResult=  movementApi.findByUserId(uid,state,page,pagesize);
        //获取到动态的信息
        List<Movement> items = (List<Movement>) pageResult.getItems();
        //判断当前用户动态是否为空
        if(CollUtil.isEmpty(items)){
            return new PageResult();
        }
        //如果不为空,获取到用户的id
        List<Long> userId = CollUtil.getFieldValues(items, "userId", Long.class);
        //根据id去查询详细信息
        Map<Long, UserInfo> map = userInfoApi.finByIds(userId, null);
        List<MovementsVo> vos = new ArrayList<>();
        for (Movement movement : items) {
            UserInfo userInfo = map.get(movement.getUserId());
            if(userInfo != null) {
                MovementsVo vo = MovementsVo.init(userInfo, movement);
                vos.add(vo);
            }
        }
        //4、构造返回值
        pageResult.setItems(vos);
        return pageResult;
    }

    /**
     * 用户冻结
     * @param params
     * @return
     */
    public Map userFreeze(Map params) {
        //获取到要冻结的用户id
        String userId = params.get("userId").toString();
        //把冻结的信息保存到redis里面
        //拼接key
        String key=Constants.USER_FREEZE + userId;
        //获取到冻结的时间
        //冻结时间，1为冻结3天，2为冻结7天，3为永久冻结
       Integer freezingTime= Integer.valueOf(params.get("freezingTime").toString());
       //定义一个day来判断冻结的时间
        int day=0;
        if(freezingTime==1){
            day=3;
        }else if(freezingTime==2){
            day=7;
        }
        //因为前端传来的json数据保存到map集合中,redis需要的是String类型,进行转换
        String value = JSON.toJSONString(params);
        //把key和value保存到redis中
        //如果day>0说明冻结的有时间限制
        if(day>0){
            //便于测试,设置时间为分钟
            redisTemplate.opsForValue().set(key,value,day, TimeUnit.MINUTES);
        }else {
            //无期限冻结
            redisTemplate.opsForValue().set(key,value);
        }
        //返回前端message字符串,保存到集合中
        Map map=new HashMap();
        map.put("message","冻结成功");
        return map;
    }

    /**
     * 用户解冻
     * @param params
     * @return
     */
    public Map userUnfreeze(Map params) {
        //获取到要解冻的用户id
        String userId = params.get("userId").toString();
        //拼接key
        String key=Constants.USER_FREEZE + userId;
        //删除redis数据
        redisTemplate.delete(key);
        Map retMap = new HashMap();
        retMap.put("message","解冻成功");
        return retMap;
    }
}
