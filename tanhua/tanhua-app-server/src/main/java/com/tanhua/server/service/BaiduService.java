package com.tanhua.server.service;

import api.UserlocationApi;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import vo.ErrorResult;

@Service
public class BaiduService {

    @DubboReference
    private UserlocationApi userlocationApi;

    /**
     * 上报地理信息
     * @param latitude 纬度
     * @param longitude 经度
     * @param addrStr 位置描述
     */
    public void updateLocation(Double latitude, Double longitude, String addrStr) {
        //把用户信息更新到MongoDB数据库
     boolean flag= userlocationApi.updateLocation(UserHolder.getUserId(),latitude,longitude,addrStr);
        if(!flag){
            throw new BusinessException(ErrorResult.error());
        }


    }
}
