package com.tanhua.server.service;

import api.FriendApi;
import api.UserApi;
import api.UserInfoApi;
import cn.hutool.core.collection.CollUtil;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import domain.User;
import domain.UserInfo;
import domain.UserInfoVo;
import mongo.Friend;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utils.Constants;
import vo.ContactVo;
import vo.ErrorResult;
import vo.PageResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MessagesService {

    @DubboReference
    private UserApi userApi;
    @DubboReference
    private UserInfoApi userInfoApi;
    @DubboReference
    private FriendApi friendApi;
    @Autowired
    private HuanXinTemplate huanXinTemplate;

    /**
     * 根据环信id查询用户详情
     * huanxinId 环信id
     */
    public UserInfoVo findUserInfoByHuanxin(String huanxinId) {
        //根据环信id查询用户
      User user= userApi.findhuanxinId(huanxinId);
        //根据用户id查询用户详情
        UserInfo userInfo = userInfoApi.findById(user.getId());
        UserInfoVo userInfoVo=new UserInfoVo();
        if(userInfo.getAge() != null) {
            userInfoVo.setAge(userInfo.getAge().toString());
        }
        return userInfoVo;
    }

    /**
     * 添加好友
     * @param friendId 要添加的好友的id
     */
    public void contacts(Long friendId) {
        //获取到当前id
        Long userId = UserHolder.getUserId();
        //通过环信添加好友关系
        Boolean aBoolean = huanXinTemplate.addContact(Constants.HX_USER_PREFIX + userId,
                Constants.HX_USER_PREFIX + friendId);
        //判断是否添加成功
        if(!aBoolean){
            throw  new BusinessException(ErrorResult.error());
        }
        //注册成功之后在数据库添加好友之间的关系
        friendApi.save(userId,friendId);
    }

    /**
     * 删除好友
     * @param friendId 要删除的好友id
     */
    public void delete(Long friendId){
        //获取到当前id
        Long userId = UserHolder.getUserId();
        //通过环信删除好友关系
        Boolean aBoolean = huanXinTemplate.deleteContact(Constants.HX_USER_PREFIX + userId,
                Constants.HX_USER_PREFIX + friendId);
        //判断是否删除成功
        if(!aBoolean){
            //如果删除不成功的话
            throw  new BusinessException(ErrorResult.error());
        }
        //在数据库进行删除
        friendApi.delete(friendId,userId);
    }



    /**
     * 好友列表显示
     * @param page
     * @param pagesize
     * @param keyword 搜索关键字
     * @return
     */
    public PageResult findcontacts(Integer page, Integer pagesize, String keyword) {
        //根据当前用户id查询好友列表
        List<Friend> list=friendApi.findcontacts(page,pagesize,UserHolder.getUserId());
        if(CollUtil.isEmpty(list)){
        return new PageResult();
        }
        //取出好友的id
        List<Long> friendId = CollUtil.getFieldValues(list, "friendId", Long.class);
        //调用UserInfoAPI查询好友的用户详情
        UserInfo info = new UserInfo();
        //根据条件查询
        info.setNickname(keyword);
        Map<Long, UserInfo> map = userInfoApi.finByIds(friendId, info);
        //创建vo类
        List<ContactVo> vo=new ArrayList<>();
        //去查询好友的详细信息
        for (Friend friend : list) {
            UserInfo userInfo = map.get(friend.getFriendId());
            if(userInfo!=null){
                ContactVo contactVo = ContactVo.init(userInfo);
                vo.add(contactVo);
            }
        }

        return new PageResult(page,pagesize,0,vo);
    }
}
