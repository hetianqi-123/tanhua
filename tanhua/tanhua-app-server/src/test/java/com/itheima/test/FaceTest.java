package com.itheima.test;

import com.baidu.aip.face.AipFace;
import com.tanhua.AppServerApplication;
import com.tanhua.autoconfig.template.AipFaceTemplate;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class FaceTest {

    @Autowired
    private AipFaceTemplate aipFaceTemplate;

    @Test
    public void test1(){
        String url="https://tanhua1-2.oss-cn-beijing.aliyuncs.com/1.png";
        boolean detect = aipFaceTemplate.detect(url);
        System.out.println(detect);
    }

//    //设置APPID/AK/SK
//    public static final String APP_ID = "25073814";
//    public static final String API_KEY = "lh76fiQOBBKKjCGjpKdXxuBl";
//    public static final String SECRET_KEY = "K6GwpZQw5Mk6gzm7BK181eYP3tv6hwBV";
//设置APPID/AK/SK
public static final String APP_ID = "24494619";
    public static final String API_KEY = "OtWE3ZfBAsKZRhm39YU2p7la";
    public static final String SECRET_KEY = "FiwvVna5w9PWh64zmadfo2TMLV6pPgau";


    public static void main(String[] args) {
        // 初始化一个AipFace
        AipFace client = new AipFace(APP_ID, API_KEY, SECRET_KEY);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        // 调用接口
        String image ="https://tanhua1-2.oss-cn-beijing.aliyuncs.com/1.png";
        String imageType = "URL";


        HashMap<String, String> options = new HashMap<String, String>();
        options.put("face_field", "age");
        options.put("max_face_num", "2");
        options.put("face_type", "LIVE");
        options.put("liveness_control", "LOW");

        // 人脸检测
        JSONObject res = client.detect(image, imageType, options);
        System.out.println(res.toString(2));
        Object error_code = res.get("error_code");
        System.out.println(error_code);
    }




}