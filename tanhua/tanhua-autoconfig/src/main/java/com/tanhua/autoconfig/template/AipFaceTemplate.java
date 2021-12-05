package com.tanhua.autoconfig.template;


import com.baidu.aip.face.AipFace;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * 检测图片中是否包含人脸
 * 包含为true
 * 不包含false
 */
@Component
public class AipFaceTemplate {

    @Autowired
    private AipFace client;

    public boolean detect(String imageUrl){

        // 调用接口
        String imageType = "URL";
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("face_field", "age");
        options.put("max_face_num", "2");
        options.put("face_type", "LIVE");
        options.put("liveness_control", "LOW");

        // 人脸检测
        JSONObject res = client.detect(imageUrl, imageType, options);
        System.out.println(res.toString(2));

        Integer error_code = (Integer) res.get("error_code");
       // System.out.println(error_code);
        return error_code==0;
    }
}
