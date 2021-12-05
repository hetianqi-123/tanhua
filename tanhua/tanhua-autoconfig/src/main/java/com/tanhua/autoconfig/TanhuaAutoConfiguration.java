package com.tanhua.autoconfig;

import com.tanhua.autoconfig.properties.*;
import com.tanhua.autoconfig.template.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


//EnableConfigurationProperties是对ConfigurationProperties的支持
//会自动去找到ConfigurationProperties注解里面的类，加入到Ioc容器里面
@EnableConfigurationProperties({
        SmsProperties.class,
        OssProperties.class,
        AipFaceProperties.class,
        HuanXinProperties.class,
        GreenProperties.class
})
public class TanhuaAutoConfiguration {
    //在创建发送短信方法的时候只需要把配置短信参数的类给到发送短信类的构造方法里面就可以
    @Bean
    public SmsTemplate smsTemplate(SmsProperties smsProperties){
        return  new SmsTemplate(smsProperties);
    }
    @Bean
    public OssTemplate ossTemplate(OssProperties ossProperties){
        return new OssTemplate(ossProperties);
    }
    @Bean
    public AipFaceTemplate aipFaceTemplate(){
        return new AipFaceTemplate();
    }
    @Bean
    public HuanXinTemplate huanXinTemplate(HuanXinProperties huanXinProperties){
        return new HuanXinTemplate(huanXinProperties);
    }
    @Bean
    //检测配置文件中是否具有tanhua.green开头的配置
    //同时enable属性=true
    @ConditionalOnProperty(prefix = "tanhua.green",value = "enable", havingValue = "true")
    public AliyunGreenTemplate aliyunGreenTemplate(GreenProperties greenProperties){
        return new AliyunGreenTemplate(greenProperties);
    }

}
