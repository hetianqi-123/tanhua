package com.tanhua.autoconfig.properties;

import com.baidu.aip.face.AipFace;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Data
@ConfigurationProperties(prefix = "tanhua.aip")
public class AipFaceProperties {
    private String appId;
    private String apiKey;
    private String secretKey;

    //官方建议这个对象是单列，所以对象放到容器里面
    @Bean
    public AipFace aipFace() {
        AipFace client = new AipFace(appId, apiKey, secretKey);
        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
        return client;
    }

}
