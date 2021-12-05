package com.tanhua.recommend.listener;

import com.alibaba.fastjson.JSON;
import mongo.Video;
import mongo.VideoScore;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RecommendVideoListener {
    @Autowired
    private MongoTemplate mongoTemplate;

    //视频推荐消息监听
    @RabbitListener(bindings = @QueueBinding(
            //消息队列
            value = @Queue(value = "tanhua.video.queue",
                        durable = "true"),
            exchange = @Exchange(
                    value = "tanhua.log.exchange",
                    //路由类型
                    type = ExchangeTypes.TOPIC
            ),
            key = {"log.video"}
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
        //视频id   busId
        String videoId = (String) map.get("busId");
        //根据video id查询动态的pid
        Video video = mongoTemplate.findById(videoId, Video.class);
        if(video != null) {
            VideoScore vs = new VideoScore();
            vs.setUserId(userId);
            vs.setDate(System.currentTimeMillis());
            vs.setVideoId(video.getVid());
            vs.setScore(getScore(type));
            mongoTemplate.save(vs);
        }

    }

    private static Double getScore(String type) {
        //0301为发小视频，0302为小视频点赞，0303为小视频取消点赞，0304为小视频评论
        Double score = 0d;
        switch (type) {
            case "0301":
                score=2d;
                break;
            case "0302":
                score=5d;
                break;
            case "0303":
                score = -5d;
                break;
            case "0304":
                score = 10d;
                break;
            default:
                break;
        }
        return score;
    }
}


