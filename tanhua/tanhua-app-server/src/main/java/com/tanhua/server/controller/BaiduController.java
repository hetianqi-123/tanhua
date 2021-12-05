package com.tanhua.server.controller;

import com.tanhua.server.service.BaiduService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("baidu")
public class BaiduController {
    @Autowired
    private BaiduService baiduService;
    /**
     * 上报地理信息
     * @param map
     * @return
     */
    @PostMapping("location")
    public ResponseEntity  updateLocation(@RequestBody Map map){
        //纬度
        Double latitude = Double.valueOf(map.get("latitude").toString());
        //经度
        Double longitude = Double.valueOf(map.get("longitude").toString());
        //位置描述
        String addrStr =(String) map.get("addrStr");
        //保存或者更新地理信息
        baiduService.updateLocation(latitude,longitude,addrStr);
        return  ResponseEntity.ok(null);

    }
}
