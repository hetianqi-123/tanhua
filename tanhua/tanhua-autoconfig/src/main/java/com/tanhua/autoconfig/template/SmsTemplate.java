package com.tanhua.autoconfig.template;


import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.dysmsapi20170525.models.SendSmsResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.tanhua.autoconfig.properties.SmsProperties;

//发送短信的工具类
public class SmsTemplate {


    //定义配置短信参数的类为私有成员
    private SmsProperties smsProperties;
    //定义私有的带参数构造方法
    public SmsTemplate(SmsProperties smsProperties){
    this.smsProperties=smsProperties;
    }

    /**
     * 发送短信的方法
     * @param mobile 手机号
     * @param code   验证码
     */
    public void sendSms(String mobile,String code){

        try {
//            String accessKeyId = "LTAI5tMDM9pQvqbiQaMmEn7c";
//            String accessKeySecret= "0VgY4rQmuKvg7q7QGMLoT1tht4QiHA";

            //配置阿里云
            Config config = new Config()
                    // 您的AccessKey ID
                    .setAccessKeyId(smsProperties.getAccessKey())
                    // 您的AccessKey Secret
                    .setAccessKeySecret(smsProperties.getSecret());
            // 访问的域名
            config.endpoint = "dysmsapi.aliyuncs.com";

            com.aliyun.dysmsapi20170525.Client client =  new com.aliyun.dysmsapi20170525.Client(config);

            SendSmsRequest sendSmsRequest = new SendSmsRequest()
                    .setPhoneNumbers(mobile)
                    .setSignName(smsProperties.getSignName())
                    .setTemplateCode(smsProperties.getTemplateCode())
                    .setTemplateParam("{\"code\":\""+code+"\"}");
            // 复制代码运行请自行打印 API 的返回值
            SendSmsResponse response = client.sendSms(sendSmsRequest);
            SendSmsResponseBody body = response.getBody();
            System.out.println(body.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
