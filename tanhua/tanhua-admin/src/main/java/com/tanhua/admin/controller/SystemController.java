package com.tanhua.admin.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import com.tanhua.admin.service.AdminService;
import mongo.AdminVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utils.Constants;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/system/users")
public class SystemController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 生成验证码
     * @param uuid 浏览器生成随机id
     * @param response
     * @throws IOException
     */
    @GetMapping("verification")
    public void verification(String uuid, HttpServletResponse response) throws IOException {
        //通过胡图工具生成验证码
        CircleCaptcha circleCaptcha = CaptchaUtil.createCircleCaptcha(299, 97);
        //验证码存入redis
        String code =circleCaptcha.getCode();
        redisTemplate.opsForValue().set(Constants.CAP_CODE+uuid,code);
        //输出验证码图片
        circleCaptcha.write(response.getOutputStream());
    }

    /**
     * 用户登录：
     */
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody Map map) {
        Map retMap = adminService.login(map);
        return ResponseEntity.ok(retMap);
    }

    /**
     * 获取用户的信息
     */
    @PostMapping("/profile")
    public ResponseEntity profile() {
        AdminVo vo = adminService.profile();
        return ResponseEntity.ok(vo);
    }
}
