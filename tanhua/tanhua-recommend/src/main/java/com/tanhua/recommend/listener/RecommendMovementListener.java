package com.tanhua.recommend.listener;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import mongo.Movement;
import mongo.MovementScore;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

//动态推荐监听器
@Component
public class RecommendMovementListener {

    @Autowired
    private MongoTemplate mongoTemplate;

    //接收动态推荐数据的监听器
    @RabbitListener(bindings = @QueueBinding(
            //消息队列名称
            value = @Queue(
                    value ="tanhua.movement.queue",
                    durable = "true"
            ),
            //交换机
            exchange = @Exchange(
                    name = "tanhua.log.exchange",
                    //交换机类型
                    type = ExchangeTypes.TOPIC
            ),
            //路由键
            key = {"log.movement"}
    ))
    public void recommend(String message){
        //接收消息队列发送的消息
        System.out.println("处理动态消息："+message);
        //发送过来的是json字符串,转换为map集合
        Map map = JSON.parseObject(message, Map.class);
        //从map中获取数据
        // 用户id
        Long userId = Long.valueOf(map.get("userId").toString());
        //类型
        String type = (String) map.get("type");
        //时间
        String logTime = (String) map.get("logTime");
        //动态id   busId
        String movementId = (String) map.get("busId");
        //根据movementId动态id查询动态的pid
        Movement movement = mongoTemplate.findById(movementId, Movement.class);
        //判断动态是否存在
        if(movement!=null){
            //构造MovementScore对象
            MovementScore movementScore=new MovementScore();
            //用户id
            movementScore.setUserId(userId);
            //动态的pid    从movement对象中获取
            movementScore.setMovementId(movement.getPid());
            //得分
            movementScore.setScore(getScore(type,movement));
            //时间戳
            movementScore.setDate(System.currentTimeMillis());
            mongoTemplate.save(movementScore);

        }
    }

    //根据用户的行为,判断得分
    public static Double getScore(String type,Movement movement){
        //0201为发动态  基础5分 50以内1分，50~100之间2分，100以上3分
        //0202为浏览动态， 1
        //0203为动态点赞， 5
        //0204为动态喜欢， 8
        //0205为评论，     10
        //0206为动态取消点赞， -5
        //0207为动态取消喜欢   -8
        Double score =0d;
        switch (type){
            case "0201":
                score=5d;
                //如果发送的动态有图片,一张图片加一分
                score+= movement.getMedias().size();
                //如果发送的有文字,利用hutu工具判断文字的字数
                int length = StrUtil.length(movement.getTextContent());
                if(length >= 0 && length < 50){
                    score += 1;
                }else if (length < 100) {
                    score += 2;
                } else {
                    score += 3;
                }
                break;
            case "0202":
                score = 1d;
                break;
            case "0203":
                score = 5d;
                break;
            case "0204":
                score = 8d;
                break;
            case "0205":
                score = 10d;
                break;
            case "0206":
                score = -5d;
                break;
            case "0207":
                score = -8d;
                break;
            default:
                break;
        }
        return score;

    }

}
