package com.tanhua.server.controller;

import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.service.SettingsService;
import com.tanhua.server.service.TanHuaService;
import com.tanhua.server.service.UserInfoService;
import com.tanhua.server.service.UserService;
import domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vo.PageResult;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("users")
public class UsersController {

    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private UserService userService;


    //用户数据回显
    @GetMapping()
    public ResponseEntity UsersControler(Long userID,@RequestHeader("Authorization") String token){
//        //获取用户id
//        Claims claims = JwtUtils.getClaims(token);
//        Integer id = (Integer) claims.get("id");
        //判断id是否为空
        if(userID==null){
            userID=UserHolder.getUserId();
        }
        UserInfoVo userInfovo= userInfoService.findById(userID);
        return ResponseEntity.ok(userInfovo);
    }

    /**
     * 用户数据更新
     */
    @PutMapping
    public ResponseEntity updateUserInfo(@RequestHeader("Authorization") String token,
                                         @RequestBody UserInfo userInfo){
        //获取id
//        Claims claims = JwtUtils.getClaims(token);
//        Integer id = (Integer) claims.get("id");

        userInfo.setId(UserHolder.getUserId());
        userInfoService.update(userInfo);
        return ResponseEntity.ok(null);
    }

    /**
     * 完善资料更新用户头像
     */
    @PostMapping("/header")
    public ResponseEntity updateavatar(@RequestHeader("Authorization") String token,
                                        MultipartFile headPhoto) throws IOException {
        //解析token，获取id
//        Claims claims = JwtUtils.getClaims(token);
//        Integer id = (Integer)claims.get("id");
        userInfoService.updateHead(headPhoto,UserHolder.getUserId());
        return ResponseEntity.ok(null);
    }

    /**
     * 用户通用设置 - 读取
     * @return
     */
    @GetMapping("/settings")
    public ResponseEntity getSettings(){
        SettingsVo settingsVo= settingsService.settings();
        return ResponseEntity.ok(settingsVo);
    }

    /**
     * 设置陌生人问题 - 保存
     * @return
     */
    @PostMapping("/questions")
    public ResponseEntity setSettings(@RequestBody Map map){
        String content=(String) map.get("content");
        //设置陌生人问题
        settingsService.questionSettings(content);
        return ResponseEntity.ok(null);

    }


    /**
     * 通知设置 - 保存
     * @return
     */
    @PostMapping("/notifications/setting")
    public ResponseEntity tzsettings(@RequestBody Map map){
        //获取参数
        settingsService.tzsettings(map);
        return ResponseEntity.ok(null);
    }

    /**
     * 黑名单 - 翻页列表
     * @param page 当前页
     * @param pagesize 每页显示的条数
     * @return
     * 如果前端每页传，就默认从第一页，每页显示10条数据
     */
    @GetMapping("blacklist")
    public ResponseEntity fanye(@RequestParam(defaultValue = "1") int  page,
                                @RequestParam(defaultValue = "10") int pagesize){
        PageResult pr= settingsService.blacklist(page,pagesize);
        return ResponseEntity.ok(pr);
    }

    /**
     * 取消黑名单
     * @param blackUserId 要移除的黑名单用户id
     * @return
     */
    @DeleteMapping("blacklist/{uid}")
    public ResponseEntity deletefanye(@PathVariable("uid") Long blackUserId){
        settingsService.deletefanye(blackUserId);
        return ResponseEntity.ok(null);
    }


    /**
     * 修改手机号- 1 发送短信验证码
     * @return
     */
    @PostMapping("phone/sendVerificationCode")
    public ResponseEntity sencode(){
        //获取到当前的手机号
        String mobile = UserHolder.getMobile();
        System.out.println("修改验证码的手机号"+mobile);
        //调用之前的发送验证码的业务层
        userService.sendMsg(mobile);
        return  ResponseEntity.ok(null);
    }
    /**
     * 修改手机号-2  校验验证码
     */
    @PostMapping("phone/checkVerificationCode")
    public ResponseEntity checkcode(@RequestBody Map map){
        //获取到当前用户手机号
        String mobile = UserHolder.getMobile();
        //获取到当前验证码
        String code = (String) map.get("verificationCode");
        //校验
        verification  vr= settingsService.checkcode(mobile,code);
        return ResponseEntity.ok(vr);
    }

    /**
     * 修改手机号 - 3 保存
     */
    @PostMapping("phone")
    public ResponseEntity save(@RequestBody Map map){
        //获取到新的手机号
        String phone = (String)map.get("phone");
        //在业务层进行判断，新手机号是否存在
        settingsService.isphone(phone);
        return ResponseEntity.ok(null);
    }


    @Autowired
    private TanHuaService tanHuaService;
    /**
     * 互相喜欢，喜欢，粉丝 - 统计
     * @return
     */
    @GetMapping("counts")
    public ResponseEntity countslike(){
        Count count=tanHuaService.countslike();
        return ResponseEntity.ok(count);
    }


    /**
     * 互相喜欢、喜欢、粉丝、谁看过我 - 翻页列表
     * @param type
     * @param page
     * @param pagesize
     * @param nickname
     * @return
     */
    @GetMapping("friends/{type}")
    public ResponseEntity countslikelist(@PathVariable("type")int type,
                                         @RequestParam(defaultValue = "1")int page,
                                         @RequestParam(defaultValue = "10")int pagesize,
                                         String nickname){
        PageResult pageResult=tanHuaService.countslikelist(type,page,pagesize,nickname);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 取消喜欢
     * @param uid 用户id
     * @return
     */
    @DeleteMapping("like/{uid}")
    public ResponseEntity CancelLike(@PathVariable("uid") Long uid){
        //取消喜欢
        tanHuaService.CancelLike(uid);
        return  ResponseEntity.ok(null);
    }


    /**
     * 粉丝 - 喜欢
     * uid 喜欢的粉丝id
     */
    @PostMapping("fans/{uid}")
    public ResponseEntity like(@PathVariable("uid") Long uid){
        //粉丝喜欢
        tanHuaService.like(uid);
        return  ResponseEntity.ok(null);
    }


}
