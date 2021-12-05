package com.tanhua.dubbo.api;


import api.UserApi;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.dubbo.mappers.UserMapper;
import domain.User;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class UserApiImpl  implements UserApi {

    @Autowired
    private UserMapper userMapper;

    //根据手机号码查询用户
    public User findByMobile(String mobile) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("mobile",mobile);
        return userMapper.selectOne(qw);
    }

    @Override
    public Long save(User user) {
        userMapper.insert(user);
        return user.getId();
    }

    /**
     * 更新手机号
     * @param user
     */
    @Override
    public void update(User user) {
        userMapper.updateById(user);
    }

    /**
     * 根据id查询用户的注册信息
     * @param userId
     * @return
     */
    @Override
    public User findByid(Long userId) {
        User user = userMapper.selectById(userId);
        return user;
    }

    /**
     * 根据环信id查询用户注册信息
     * @param huanxinId
     * @return
     */
    @Override
    public User findhuanxinId(String huanxinId) {
        QueryWrapper<User> qw=new QueryWrapper<>();
        qw.eq("hx_user",huanxinId);
       return userMapper.selectOne(qw);
    }
}
