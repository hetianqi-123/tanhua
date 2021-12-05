package com.tanhua.server.service;

import api.UserInfoApi;
import com.tanhua.autoconfig.template.AipFaceTemplate;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.server.exception.BusinessException;
import domain.UserInfo;
import domain.UserInfoVo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vo.ErrorResult;

import java.io.IOException;

@Service
public class UserInfoService {

    //远程接收服务
    @DubboReference
    private UserInfoApi userInfoApi;
    //上传阿里云的工具类
    @Autowired
    private OssTemplate ossTemplate;
    //人脸识别的工具类
    @Autowired
    private AipFaceTemplate aipFaceTemplate;
    /**
     * 用户首次注册保存信息的方法
     * @param userInfo
     */
    public void save(UserInfo userInfo) {
        userInfoApi.save(userInfo);
    }

    //首次注册添加头像的方法
    public void updateHead(MultipartFile headPhoto,Long id) throws IOException {
        //1.将图片上传到阿里云,返回图片的下载路径
        String url = ossTemplate.upload(headPhoto.getOriginalFilename(), headPhoto.getInputStream());
        //2.将阿里云的下载路径放到人脸识别判断是否包含人脸
        boolean b = aipFaceTemplate.detect(url);
        //如果不包含人脸，报错误
        if(!b){
            throw new BusinessException(ErrorResult.faceError());
        }else {
            //如果包含人脸，保存到数据库
            //创建userInfo对象，把id和图片路径更新到数据库
            UserInfo userInfo=new UserInfo();
            userInfo.setId(Long.valueOf(id));
            userInfo.setAvatar(url);
            userInfoApi.update(userInfo);
        }

    }

    //根据id查询用户信息
    public UserInfoVo findById(Long id) {
        UserInfo userInfo = userInfoApi.findById(id);
        UserInfoVo userInfoVo=new UserInfoVo();
        BeanUtils.copyProperties(userInfo,userInfoVo);
        return userInfoVo;
    }

    //更新用户数据
    public void update(UserInfo userInfo) {
        userInfoApi.update(userInfo);
    }
}
