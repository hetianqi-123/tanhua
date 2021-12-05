package com.tanhua.server.controller;

import com.tanhua.server.service.CommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vo.PageResult;

import java.util.Map;

@RestController
@RequestMapping("comments")
public class CommentsController {

    @Autowired
    private CommentsService commentsService;

    /**
     * 评论-提交
     *  movementId 动态编号
     * comment 评论
     * @return
     */
    @PostMapping
    public ResponseEntity saveComments(@RequestBody Map map){
        //动态编号
        String movementId = (String) map.get("movementId");
        //评论
        String comment = (String) map.get("comment");

        commentsService.saveComments(movementId,comment);
        return  ResponseEntity.ok(null);
    }

    /**
     * 评论列表
     * @param movementId 动态编号
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping
    public ResponseEntity findComments(String movementId,@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "5")int pagesize){
      PageResult pageResult= commentsService.findComments(movementId,page,pagesize);

        return ResponseEntity.ok(pageResult);

}

    /**
     * 评论点赞
     * @param id 评论id
     * @return
     */
    @GetMapping("{id}/like")
    public ResponseEntity dtdz(@PathVariable("id") String id){
        Integer integer=  commentsService.pldz(id);
        return ResponseEntity.ok(integer);
    }


    /**
     * 评论取消点赞
     * @param id 评论id
     * @return
     */
    @GetMapping("{id}/dislike")
    public ResponseEntity disdtdz(@PathVariable("id") String id){
        Integer integer=  commentsService.dislikepldz(id);
        return ResponseEntity.ok(integer);
    }


}
