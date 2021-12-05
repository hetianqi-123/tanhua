package com.tanhua.dubbo.api;

import api.BlackListApi;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.dubbo.mappers.BlackListMapper;
import com.tanhua.dubbo.mappers.UserInfoMapper;
import domain.BlackList;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

//暴露服务
@DubboService
public class BlackListApiImpl  implements BlackListApi {
    @Autowired
    private BlackListMapper blackListMapper;
    @Autowired
   private UserInfoMapper userInfoMapper;

    /**
     * 分页查询的方法
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public IPage findByUserId(Long userId, int page, int pagesize) {
        //创建分页对象
        Page pages = new Page(page,pagesize);
        //2、调用方法分页（自定义编写 分页参数Page，sql条件参数）
        //因为要查询的是被拉黑的用户的详细信息，所以要在userInfoMapper进行查询
        return userInfoMapper.findBlackList(pages,userId);
    }

    /**
     * 移除黑名单
     * @param blackUserId 移除的黑名单用户id
     * @param userId  当前登录系统的用户id
     */
    @Override
    public void deletefanye(Long blackUserId, Long userId) {
        QueryWrapper<BlackList> qw=new QueryWrapper<>();
        qw.eq("black_user_id",blackUserId);
        qw.eq("user_id",userId);
        blackListMapper.delete(qw);
    }


}
