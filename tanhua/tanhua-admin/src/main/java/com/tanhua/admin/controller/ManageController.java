package com.tanhua.admin.controller;

import com.tanhua.admin.service.ManagerService;
import domain.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vo.PageResult;

import java.util.Map;

@RestController
@RequestMapping("manage")
public class ManageController {

    @Autowired
    private ManagerService managerService;

    /**
     * 查询用户列表
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("users")
    public ResponseEntity users(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pagesize){
        PageResult result = managerService.findAllUsers(page,pagesize);
        return ResponseEntity.ok(result);
    }


    /**
     * 查询单个用户的详细信息
     * @param userId
     * @return
     */
    @GetMapping("users/{userID}")
    public ResponseEntity findById(@PathVariable("userID") Long userId){
        UserInfo userInfo = managerService.findById(userId);
        return ResponseEntity.ok(userInfo);
    }



    /**
     * 查询指定用户发布的所有视频列表
     */
    @GetMapping("/videos")
    public ResponseEntity videos(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer pagesize,
                                 Long uid ) {
        PageResult result = managerService.findAllVideos(page,pagesize,uid);
        return ResponseEntity.ok(result);
    }


    /**
     * 查询动态列表
     * @param page
     * @param pagesize
     * @param uid 用户ID
     * @param state 审核状态
     * @return
     */
    @GetMapping("/messages")
    public ResponseEntity messages(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pagesize,
                                   Long uid,Integer state ) {
        PageResult result = managerService.findAllMovements(page,pagesize,uid,state);
        return ResponseEntity.ok(result);
    }


    /**
     * 用户冻结
     * @param params
     * @return
     */
    @PostMapping("/users/freeze")
    public ResponseEntity freeze(@RequestBody Map params) {
        Map map =  managerService.userFreeze(params);
        return ResponseEntity.ok(map);
    }


    //用户解冻
    @PostMapping("/users/unfreeze")
    public ResponseEntity unfreeze(@RequestBody  Map params) {
        Map map =  managerService.userUnfreeze(params);
        return ResponseEntity.ok(map);
    }










}
