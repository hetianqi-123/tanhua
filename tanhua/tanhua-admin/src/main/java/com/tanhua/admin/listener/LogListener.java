package com.tanhua.admin.listener;

import com.alibaba.fastjson.JSON;
import com.tanhua.admin.mapper.LogMapper;
import domain.Log;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
//接收日志消息
@Component
public class LogListener {

    @Autowired
    private LogMapper logMapper;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                         value = "tanhua.log.queue",
                            durable = "true"
                    ),
                    exchange = @Exchange(
                            value = "tanhua.log.exchange",
                            type = ExchangeTypes.TOPIC
                    ),
                    key = "log.*"
            )
    )
    public void log(String message){
    //把消息转化为map类型
        try {
            Map map = JSON.parseObject(message, Map.class);
            map.forEach((k,v)->System.out.println(k+"--"+v));
            //解析map获取数据
            Long userId = Long.valueOf(map.get("userId").toString());
            String type = (String) map.get("type");
            String logTime = (String)map.get("logTime");
            //构造log对象,保存到数据库中
            Log log=new Log(userId,logTime,type);
            logMapper.insert(log);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}