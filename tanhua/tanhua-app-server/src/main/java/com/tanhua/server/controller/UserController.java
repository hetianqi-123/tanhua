package com.tanhua.server.controller;

import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.service.UserInfoService;
import domain.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserInfoService userInfoService;

    /**
     * 保存用户首次注册的信息
     * @param UserInfo
     * @param token
     * @return
     */
    @PostMapping("/loginReginfo")
    public ResponseEntity loginReginfo(@RequestBody UserInfo UserInfo,
            @RequestHeader("Authorization")String token){

//        //解析token
//        Claims claims = JwtUtils.getClaims(token);
//        //获取token中的id
//        Integer id = (Integer) claims.get("id");
//        //因为前端传来的UserInfo没有包含id,所以把id写入到UserInfo中
//        UserInfo.setId(Long.valueOf(id));
        UserInfo.setId(UserHolder.getUserId());
        //调用保存的方法
        userInfoService.save(UserInfo);
        return ResponseEntity.ok(null);
    }

    /**
     * 注册时修改用户头像
     * @param headPhoto
     * @param token
     * @return
     * @throws Exception
     */
    @PostMapping("/loginReginfo/head")
    public ResponseEntity head(MultipartFile headPhoto,
                               @RequestHeader("Authorization") String token )throws Exception{

        //解析token，获取ibb
//        Claims claims = JwtUtils.getClaims(token);
//        Integer id = (Integer)claims.get("id");
        userInfoService.updateHead(headPhoto,UserHolder.getUserId());
        return ResponseEntity.ok(null);
    }












}
