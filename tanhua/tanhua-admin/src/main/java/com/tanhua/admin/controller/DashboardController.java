package com.tanhua.admin.controller;

import com.tanhua.admin.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vo.AnalysisUsersVo;

@RestController
@RequestMapping("dashboard")
public class DashboardController {


    @Autowired
    private AnalysisService analysisService;
    /**
     * 新增、活跃用户、次日留存率
     * 101 新增 102 活跃用户 103 次日留存率
     */
    @GetMapping("/users")
    public ResponseEntity getUsers(@RequestParam(name = "sd") Long sd
            , @RequestParam("ed") Long ed
            , @RequestParam("type") Integer type) {
        AnalysisUsersVo analysisUsersVo=  analysisService.queryAnalysisUsersVo(sd, ed, type);
        //查询数据
      return   ResponseEntity.ok(analysisUsersVo);

    }
}
