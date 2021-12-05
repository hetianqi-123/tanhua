package com.tanhua.server.controller;

import com.tanhua.server.service.TanHuaService;
import dto.RecommendUserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vo.NearUserVo;
import vo.PageResult;
import vo.TodayBest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("tanhua")
public class TanHuaController {

    @Autowired
    private TanHuaService tanHuaService;


    /**
     * 查询今日佳人
     */
    @GetMapping("todayBest")
    public ResponseEntity FindWind(){

        TodayBest todayBest=tanHuaService.FindWind();
        return ResponseEntity.ok(todayBest);
    }


    /**
     * 查询分页的推荐列表
     * @param dto
     * @return
     */
    @GetMapping("/recommendation")
    public ResponseEntity recommendation(RecommendUserDto dto){
        PageResult pr= tanHuaService.recommendation(dto);
        return ResponseEntity.ok(pr);
    }


    /**
     * 查看佳人信息
     * @param id 佳人id
     * @return
     */
    @GetMapping("{id}/personalInfo")
    public ResponseEntity findjiaren(@PathVariable("id") Long id){
        TodayBest todayBest =tanHuaService.findjiaren(id);
        return  ResponseEntity.ok(todayBest);
    }

    /**
     * 查看佳人的陌生人问题
     * @param userId 陌生人的id
     * @return
     */
    @GetMapping("strangerQuestions")
    public ResponseEntity findmswenti(Long userId){
    String wenti=tanHuaService.findmswenti(userId);
        return ResponseEntity.ok(wenti);
    }

    /**
     * 回复陌生人问题
     * @param map
     * @return
     */
    @PostMapping("strangerQuestions")
    public ResponseEntity huifuwenti(@RequestBody Map map){
        //从map中获取用户id和回复消息
        //前端传递的userId:是Integer类型的,所以要进行两次转换
        String obj = map.get("userId").toString();
        Long userId = Long.valueOf(obj);
        String reply =(String) map.get("reply");
        tanHuaService.replyQuestions(userId,reply);
        return ResponseEntity.ok(null);
    }

    /**
     * 左滑右滑功能
     * 被喜欢或者不喜欢的就不能在这里进行显示了
     * @return
     */
    @GetMapping("cards")
    public ResponseEntity zuoyouhua(){
        //根据前端需要的返回值,可以得出是TodayBest类
       List<TodayBest> list= tanHuaService.zuoyouhua();
       return  ResponseEntity.ok(list);
    }



    /**
     * 右滑喜欢
     * @param likeUserId 喜欢的用户编号
     * @return
     */
    @GetMapping("{id}/love")
    public ResponseEntity love(@PathVariable("id") Long likeUserId){
        tanHuaService.love(likeUserId);
        return  ResponseEntity.ok(null);
    }

    /**
     * 不喜欢
     */
    @GetMapping("{id}/unlove")
    public ResponseEntity<Void> notLikeUser(@PathVariable("id") Long likeUserId) {
        tanHuaService.notLikeUser(likeUserId);
        return ResponseEntity.ok(null);
    }

    /**
     *  搜索附近
     * @param gender 性别
     * @param distance 距离
     * @return
     */
    @GetMapping("search")
    public ResponseEntity search(String gender,
                                 @RequestParam(defaultValue = "10000") String distance){

        //返回多个用户信息
    List<NearUserVo> list =tanHuaService.search(gender,distance);
    return ResponseEntity.ok(list);
    }


    
    

}
