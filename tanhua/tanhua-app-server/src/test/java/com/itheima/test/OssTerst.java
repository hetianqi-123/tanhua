package com.itheima.test;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.tanhua.AppServerApplication;
import com.tanhua.autoconfig.template.OssTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 案例：
 *      将资料中的照片传到阿里云oss
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class OssTerst {

    @Autowired
    private OssTemplate ossTemplate;
    @Test
    public void test1() throws FileNotFoundException {
        //1.定义照片的路径
        String path="E:\\2.png";
        //2.创建输入流对象
        FileInputStream fileInputStream=new FileInputStream(new File(path));
        String url = ossTemplate.upload(path, fileInputStream);
        System.out.println(url);
    }





    @Test
    public void osstest() throws FileNotFoundException {

        //1.定义照片的路径
        String path="E:\\2.png";
        //2.创建输入流对象
        FileInputStream fileInputStream=new FileInputStream(new File(path));
        //3.拼写图片路径 以年月日创建路径，加uuid加后缀名
        String filename=new SimpleDateFormat("yyyy-MM-dd").format(new Date())+"/"+
                UUID.randomUUID().toString()+path.substring(path.lastIndexOf("."));

        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
        String endpoint = "oss-cn-beijing.aliyuncs.com";
        // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
        String accessKeyId = "LTAI5tAFiWj36GiscQwFFjjB";
        String accessKeySecret = "ghQwr1sEK0x2nEa6zMpNgCYPU7cPjM";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId,accessKeySecret);

        // 填写Byte数组。
        // 填写Bucket名称和Object完整路径。Object完整路径中不能包含Bucket名称。
        ossClient.putObject("tanhua1-2", filename, fileInputStream);

        // 关闭OSSClient。
        ossClient.shutdown();

        //照片的访问域名就是阿里云的域名加filename的地址
        String url= "https://tanhua1-2.oss-cn-beijing.aliyuncs.com/"+filename;
        System.out.println(url);
        }

}
