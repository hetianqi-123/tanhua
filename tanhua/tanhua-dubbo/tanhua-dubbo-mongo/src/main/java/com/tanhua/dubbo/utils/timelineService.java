package com.tanhua.dubbo.utils;

import mongo.Friend;
import mongo.MovementTimeLine;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class timelineService {

    @Autowired
    private MongoTemplate mongoTemplate;

    //异步多线程执行(另外开辟一条线程)
    @Async
    /**
     * userId当前用户id
     * movementId movement的主键id
     */
    public void saveTimeLine(Long userId, ObjectId movementId){
    //根据用户id去查询friend表的好友资料
    Criteria criteria=Criteria.where("userId").is(userId);
    Query query=Query.query(criteria);
    //查询friend表的好友id
    List<Friend> friends = mongoTemplate.find(query, Friend.class);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //遍历好友封装类
    for (Friend friend : friends) {
        if(friend!=null){
            //构建时间线表
            MovementTimeLine movementTimeLine=new MovementTimeLine();
            //保存动态id
            movementTimeLine.setMovementId(movementId);
            //保存当前用户id,也就是发布动态的用户id
            movementTimeLine.setUserId(friend.getUserId());
            //可见好友的id
            movementTimeLine.setFriendId(friend.getFriendId());
            //发布动态时间
            movementTimeLine.setCreated(System.currentTimeMillis());
            mongoTemplate.save(movementTimeLine);
        }
    }
}

}
