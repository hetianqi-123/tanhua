package com.tanhua.server.controller;

import com.tanhua.server.service.huanxinuserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vo.HuanXinUserVo;

@RestController
@RequestMapping("huanxin")
public class huanxinController {

    @Autowired
    private huanxinuserService huanxinuserService;


    @GetMapping("user")
    public ResponseEntity huanxinuser(){
        //查询之后返回环信的uservo
        HuanXinUserVo huanXinUserVo=huanxinuserService.zhuceuser();
        return ResponseEntity.ok(huanXinUserVo);
    }

}
