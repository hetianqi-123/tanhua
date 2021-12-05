package com.tanhua.server.service;

import api.UserApi;
import com.tanhua.server.interceptor.UserHolder;
import domain.User;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import vo.HuanXinUserVo;

@Service
public class huanxinuserService {
    @DubboReference
    private UserApi userApi;
    /**
     * 环信用户信息
     * @return
     */
    public HuanXinUserVo zhuceuser() {
        //获取当前用户id,根据用户id查询环信的账号密码
        Long userId = UserHolder.getUserId();
        User user= userApi.findByid(userId);
        if(user==null){
          return null;
        }
        //查询出user的环信账号密码
        return new HuanXinUserVo(user.getHxUser(),user.getHxPassword());
    }

}
