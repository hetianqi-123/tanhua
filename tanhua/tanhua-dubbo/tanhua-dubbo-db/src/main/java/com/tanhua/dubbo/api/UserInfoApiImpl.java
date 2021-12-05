package com.tanhua.dubbo.api;

import api.UserInfoApi;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.dubbo.mappers.UserInfoMapper;
import domain.UserInfo;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;


//进行暴露服务
@DubboService
public class UserInfoApiImpl implements UserInfoApi {
    @Autowired
   private UserInfoMapper userInfoMapper;
    //首次注册保存的方法
    @Override
    public void save(UserInfo userInfo) {
        userInfoMapper.insert(userInfo);
    }

    //首次注册添加人脸识别的方法
    @Override
    public void update(UserInfo userInfo) {
        userInfoMapper.updateById(userInfo);
    }

    //根据id查询用户信息
    @Override
    public UserInfo findById(Long id) {
        return userInfoMapper.selectById(id);
    }

    /**
     * 一次性查询多个userinfo
     * @param userid 推荐的用户id
     * @return
     */
    @Override
    public Map<Long, UserInfo> finByIds(List<Long> userid,UserInfo info) {
        QueryWrapper qw = new QueryWrapper();
        //1、用户id列表
        qw.in("id",userid);
        //2、添加筛选条件
        if(info != null) {
            if(info.getAge() != null) {
                qw.lt("age",info.getAge());
            }
            if(!StringUtils.isEmpty(info.getGender())) {
                qw.eq("gender",info.getGender());
            }
            if(!StringUtils.isEmpty(info.getNickname())){
                qw.like("nickname",info.getNickname());
            }
        }
        List<UserInfo> list = userInfoMapper.selectList(qw);
        Map<Long, UserInfo> map = CollUtil.fieldValueMap(list, "id");
        return map;
    }

    /**
     * 分页查询用户详细资料
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public IPage<UserInfo> findAll(Integer page, Integer pagesize) {
        //创建分页对象
        IPage<UserInfo> iPage =new Page<>(page,pagesize);
        return userInfoMapper.selectPage(iPage, null);
    }

}
