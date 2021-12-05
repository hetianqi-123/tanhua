package com.tanhua.admin.listener;

import api.MovementApi;
import com.tanhua.autoconfig.template.AliyunGreenTemplate;
import mongo.Movement;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

//接收动态信息
@Component
public class MovementListener {
    @DubboReference
    private MovementApi movementApi;
    @Autowired
    private AliyunGreenTemplate aliyunGreenTemplate;



    //通过消息接收到动态的id
    @RabbitListener(bindings=@QueueBinding(
        value = @Queue(
                //消息队列
                value="tanhua.audit.queue",
                durable = "true"
        ),
        exchange = @Exchange(
                //交换机
                value = "tanhua.audit.exchange",
                //接收类型
                type = ExchangeTypes.TOPIC) ,
        //路由键
        key = {"audit.movement"}
    ))
    public void listenCreate(String movementId) throws Exception {
        try {
            //接收到动态id,获取到动态id发布的文字和图片内容用阿里云来审核,是否违规
            //根据动态id查询动态
            Movement movement = movementApi.findMovementById(movementId);
            //只对状态是0,并且有动态数据的进行处理
            if(movement!=null && movement.getState()==0){
                //审核文本
                Map<String, String> textScan = aliyunGreenTemplate.greenTextScan(movement.getTextContent());
                //审核图片
                Map imageScan = aliyunGreenTemplate.imageScan(movement.getMedias());

                int state=0;

                //判断审核结果
                if(textScan!=null && imageScan!=null){
                    //拿到审核的结果
                    String testsuggestion = textScan.get("suggestion");
                    String imgsuggestion = textScan.get("suggestion");
                    //只要有一项审核不通过
                    if("block".equals(testsuggestion) && "block".equals(imgsuggestion)){
                        //把状态改为2驳回
                    state=2;
                    //审核通过
                    }else if("pass".equals(testsuggestion) && "pass".equals(imgsuggestion)){
                        state=1;
                    }
                }
                //更新动态
                movementApi.update(movementId,state);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
