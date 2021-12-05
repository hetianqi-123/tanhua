package com.tanhua.autoconfig.template;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.tanhua.autoconfig.properties.OssProperties;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class OssTemplate {

    //定义配置类为成员变量
    private OssProperties ossProperties;
    //创建构造方法，以配置类为参数
    public OssTemplate(OssProperties ossProperties){
        this.ossProperties=ossProperties;
    }

    public String upload(String filename, InputStream inputStream){
//        //1.定义照片的路径
//        String path="E:\\2.png";
//        //2.创建输入流对象
//        FileInputStream fileInputStream=new FileInputStream(new File(path));
        //3.拼写图片路径 以年月日创建路径，加uuid加后缀名
         filename=new SimpleDateFormat("yyyy-MM-dd").format(new Date())+"/"+
                UUID.randomUUID().toString()+filename.substring(filename.lastIndexOf("."));

        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
        String endpoint = ossProperties.getEndpoint();
        // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
        String accessKeyId = ossProperties.getAccessKey();
        String accessKeySecret = ossProperties.getSecret();

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId,accessKeySecret);

        // 填写Byte数组。
        // 填写Bucket名称和Object完整路径。Object完整路径中不能包含Bucket名称。
        ossClient.putObject(ossProperties.getBucketName(), filename, inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();

        //照片的访问域名就是阿里云的域名加filename的地址
        String url= ossProperties.getUrl()+"/"+filename;
        return url;
    }

}
