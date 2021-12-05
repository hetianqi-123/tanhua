package com.tanhua.server.controller;

import com.tanhua.server.service.CommentsService;
import com.tanhua.server.service.MovementsService;
import mongo.Movement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vo.MovementsVo;
import vo.PageResult;
import vo.VisitorsVo;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/movements")
public class movementsController {

    @Autowired
    private MovementsService movementsService;
    @Autowired
    private CommentsService commentsService;

    /** 发布-动态
     * MultipartFile是spring类型，代表HTML中form data方式上传的文件，包含二进制数据+文件名称。
     * @param movement 封装类
     * @param imageContent 文件上传参数,是个数组,可以传入多个图
     * @return
     */
    @PostMapping
    public ResponseEntity movements(Movement movement,
                                    MultipartFile imageContent[]) throws IOException {
        movementsService.movements(movement,imageContent);
        return ResponseEntity.ok(null);
    }

    /**
     * 查询个人动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @GetMapping("all")
    public ResponseEntity all(@RequestParam(defaultValue = "1")int page,
                             @RequestParam(defaultValue = "5") int pagesize,
                             @RequestParam Long userId){
        PageResult result=  movementsService.all(page,pagesize,userId);
              return ResponseEntity.ok(result);
    }

    /**
     * 查询好友动态
     * @return
     */
    @GetMapping
    public ResponseEntity friendsmovements(@RequestParam(defaultValue = "1")int page,
                                    @RequestParam(defaultValue = "5") int pagesize){

        PageResult result= movementsService.friendsmovements(page,pagesize);
        return ResponseEntity.ok(result);
    }

    /**
     * 推荐动态
     * @return
     */
    @GetMapping("recommend")
    public ResponseEntity recommended(@RequestParam(defaultValue = "1")int page,
                                      @RequestParam(defaultValue = "5") int pagesize){
        PageResult result= movementsService.recommended(page,pagesize);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询单条动态
     * @param movementId
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity findMovementById(@PathVariable("id") String movementId){
        MovementsVo mo= movementsService.findMovementById(movementId);
        return ResponseEntity.ok(mo);
    }

    /**
     * 点赞功能
     * @param movementId 动态id
     * @return
     */
    @GetMapping("{id}/like")
    public ResponseEntity like(@PathVariable("id")String movementId){
      Integer integer=  movementsService.like(movementId);
      return ResponseEntity.ok("integer");
    }


    /**
     * 取消点赞
     * @param movementId
     * @return
     */
    @GetMapping("{id}/dislike")
    public ResponseEntity dislike(@PathVariable("id")String movementId){
        Integer integer=  movementsService.dislikeComment(movementId);
        return ResponseEntity.ok("integer");
    }
    /**
     * 喜欢
     */
    @GetMapping("/{id}/love")
    public ResponseEntity love(@PathVariable("id") String movementId) {
        Integer likeCount = commentsService.loveComment(movementId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * 取消喜欢
     */
    @GetMapping("/{id}/unlove")
    public ResponseEntity unlove(@PathVariable("id") String movementId) {
        Integer likeCount = commentsService.disloveComment(movementId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * 首页谁看过我
     * @return
     */
    @GetMapping("visitors")
    public ResponseEntity queryVisitorsList(){
        List<VisitorsVo> list = movementsService.queryVisitorsList();
        return ResponseEntity.ok(list);
    }








}
