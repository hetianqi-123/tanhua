package com.tanhua.server.controller;

import com.tanhua.server.service.SmallVideosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vo.PageResult;

import java.io.IOException;

@RestController
@RequestMapping("smallVideos")
public class smallVideosController {
    @Autowired
    private SmallVideosService videosService;

    /**
     * 发布视频
     * @param videoThumbnail 视频封面文件
     * @param videoFile 视频文件
     * @return
     */
        @PostMapping
        public ResponseEntity SmallVideoController(MultipartFile videoThumbnail,MultipartFile videoFile ) throws IOException {
            videosService.saveVideos(videoThumbnail,videoFile);
            return ResponseEntity.ok(null);
        }


    /**
     * 视频列表
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping
    public ResponseEntity queryVideoList(@RequestParam(defaultValue = "1")  Integer page,
                                         @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult result = videosService.queryVideoList(page, pagesize);
        return ResponseEntity.ok(result);
    }


    /**
     * 视频用户关注
     * @param uid
     * @return
     */
    @PostMapping("{uid}/userFocus")
    public ResponseEntity guanzhu(@PathVariable("uid") Long uid){
        videosService.guanzhu(uid);
        return ResponseEntity.ok(null);
    }

    /**
     * 取消关注
     * @param uid 取消关注的用户id
     * @return
     */
    @PostMapping("{uid}/userUnFocus")
    public ResponseEntity quxiaoguanzhu(@PathVariable("uid") Long uid){
        videosService.quxiaoguanzhu(uid);
        return ResponseEntity.ok(null);
    }

    /**
     * 视频点赞
     * @param id 视频id
     * @return
     */
    @PostMapping("{id}/like")
    public ResponseEntity like(@PathVariable("id") String id){
        videosService.like(id);
        return ResponseEntity.ok(null);
    }
    /**
     * 视频取消点赞
     * @param id 视频id
     * @return
     */
    @PostMapping("{id}/dislike")
    public ResponseEntity nolike(@PathVariable("id") String id){
        videosService.nolike(id);
        return ResponseEntity.ok(null);
    }


    /**
     * 视频评论
     * @param id 视频id
     * @param comment 评论内容
     * @return
     */
    @PostMapping("{id}/comments")
    public ResponseEntity comments(@PathVariable("id") String id,
                                   @RequestBody String comment){
        videosService.comments(id,comment);
        return ResponseEntity.ok(null);
    }


//    /**
//     * 视频列表
//     * @param id 视频id
//     * @param page
//     * @param pagesize
//     * @return
//     */
//    @GetMapping("{id}/comments")
//    public ResponseEntity listcomments(@PathVariable("id") String id,
//                                       @RequestParam(defaultValue = "1")Integer page,
//                                       @RequestParam(defaultValue = "10")Integer pagesize){
//
//    }



}
