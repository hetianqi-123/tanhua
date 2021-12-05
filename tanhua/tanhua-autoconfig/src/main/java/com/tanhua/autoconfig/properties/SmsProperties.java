package com.tanhua.autoconfig.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
@Data
//读取短信验证码的配置类
@ConfigurationProperties(prefix = "tanhua.sms")
public class SmsProperties {
    private String signName;
    private String templateCode;
    private String accessKey;
    private String secret;

}
