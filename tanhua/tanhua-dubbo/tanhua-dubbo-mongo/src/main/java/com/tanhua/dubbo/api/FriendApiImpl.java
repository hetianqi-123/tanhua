package com.tanhua.dubbo.api;

import api.FriendApi;
import mongo.Friend;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class FriendApiImpl implements FriendApi {

    @Autowired
    private MongoTemplate mongoTemplate;
    /**
     * 添加好友
     * @param userId 当前用户id
     * @param friendId 好友id
     */
    @Override
    public void save(Long userId, Long friendId) {
        //关系是双向的,所以两个都要进行添加
        //保存自己的好友数据
        Query query1 = Query.query(Criteria.where("userId").is(userId).and("frinedId").is(friendId));
        //判断好友关系是否存在
        if(!mongoTemplate.exists(query1, Friend.class)) {
            //如果不存在，保存
            Friend friend1 = new Friend();
            friend1.setUserId(userId);
            friend1.setFriendId(friendId);
            friend1.setCreated(System.currentTimeMillis());
            mongoTemplate.save(friend1);
        }
        //保存好友的数据
        Query query2 = Query.query(Criteria.where("userId").is(friendId).and("frinedId").is(userId));
        //判断好友关系是否存在
        if(!mongoTemplate.exists(query2, Friend.class)) {
            //如果不存在，保存
            Friend friend1 = new Friend();
            friend1.setUserId(friendId);
            friend1.setFriendId(userId);
            friend1.setCreated(System.currentTimeMillis());
            mongoTemplate.save(friend1);
        }

    }

    /**
     * 好友列表显示
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public List<Friend> findcontacts(Integer page, Integer pagesize, Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        Query query = Query.query(criteria).skip((page - 1) * pagesize).limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        return mongoTemplate.find(query,Friend.class);
    }

    /**
     * 双发好友互相删除
     * @param friendId  要删除的好友id
     * @param userId 当前用户id
     */
    @Override
    public void delete(Long friendId, Long  userId) {
        //删除是双向的,互相删除
        Query query= Query.query(Criteria.where("userId").is(userId).and("friendId").is(friendId));
        //判断好友关系是否存在
        if(mongoTemplate.exists(query,Friend.class)){
           //进行删除
           mongoTemplate.remove(query,Friend.class);
        }
        //同样判断好友的
        Query query1= Query.query(Criteria.where("userId").is(friendId).and("frinedId").is(userId));
        if(mongoTemplate.exists(query1,Friend.class)){
            mongoTemplate.remove(query1,Friend.class);
        }

    }


}
