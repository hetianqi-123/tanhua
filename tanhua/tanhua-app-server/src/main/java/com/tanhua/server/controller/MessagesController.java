package com.tanhua.server.controller;

import com.tanhua.server.service.CommentsService;
import com.tanhua.server.service.MessagesService;
import domain.UserInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vo.PageResult;

import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessagesController {
    @Autowired
    private MessagesService messagesService;
    @Autowired
    private CommentsService commentsService;
    /**
     * 根据环信id查询用户详情
     * huanxinId 环信id
     */
    @GetMapping("/userinfo")
    public ResponseEntity userinfo(String huanxinId) {
        UserInfoVo vo = messagesService.findUserInfoByHuanxin(huanxinId);
        return ResponseEntity.ok(vo);
    }

    /**
     * 添加好友
     * @param map friendId 添加的好友id
     * @return
     */
    @PostMapping("contacts")
    public  ResponseEntity contacts(@RequestBody Map map){
        //获取用户id 因为前端传来的是Integer类型 先转换成String类型
        Long friendId = Long.valueOf(map.get("userId").toString());
        //调用service添加好友的方法
        messagesService.contacts(friendId);
        return ResponseEntity.ok(null);
    }


    /**
     * 好友列表显示
     * @param page
     * @param pagesize
     * @param keyword 搜索关键字
     * @return
     */
    @GetMapping("contacts")
    public ResponseEntity contacts(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pagesize,
                                   String keyword){
        PageResult pr=messagesService.findcontacts(page,pagesize,keyword);
        return ResponseEntity.ok(pr);
    }


    /**
     * 点赞列表
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("likes")
    public ResponseEntity findlike(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pagesize){
     PageResult pageResult=   commentsService.findlike(page,pagesize);
     return  ResponseEntity.ok(pageResult);
    }

    /**
     * 评论列表
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("comments")
    public ResponseEntity findcomments(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pagesize){
        PageResult pageResult=   commentsService.findcomments(page,pagesize);
        return  ResponseEntity.ok(pageResult);
    }


    /**
     * 喜欢列表
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("loves")
    public ResponseEntity findloves(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer pagesize){
        PageResult pageResult=   commentsService.findloves(page,pagesize);
        return  ResponseEntity.ok(pageResult);
    }


    /**
     * 公告列表
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("announcements")
    public ResponseEntity findannouncements(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer pagesize){
        PageResult pageResult=   commentsService.findannouncements(page,pagesize);
        System.out.println(pageResult.getItems());
        return  ResponseEntity.ok(pageResult);
    }


}
