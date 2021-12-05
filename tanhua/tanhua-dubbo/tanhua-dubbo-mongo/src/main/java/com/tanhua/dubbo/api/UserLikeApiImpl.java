package com.tanhua.dubbo.api;

import api.UserLikeApi;
import cn.hutool.core.collection.CollUtil;
import mongo.UserLike;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@DubboService
public class UserLikeApiImpl implements UserLikeApi {
    @Autowired
    private MongoTemplate mongoTemplate;


    /**查询喜欢表有没有这个用户
     *
     * @param userId
     * @param likeUserId
     * @param isLike 是否喜欢
     * @return
     */
    @Override
    public boolean savelike(Long userId, Long likeUserId, boolean isLike) {
        try {
            //1、查询数据
            Query query = Query.query(Criteria.where("userId").is(userId).and("likeUserId").is(likeUserId));
            UserLike userLike = mongoTemplate.findOne(query, UserLike.class);
            //2、如果不存在，保存
            if(userLike == null) {
                userLike = new UserLike();
                userLike.setUserId(userId);
                userLike.setLikeUserId(likeUserId);
                userLike.setCreated(System.currentTimeMillis());
                userLike.setUpdated(System.currentTimeMillis());
                userLike.setIsLike(isLike);
                mongoTemplate.save(userLike);
            }else {
                //3、更新
                Update update = Update.update("isLike", isLike)
                        .set("updated",System.currentTimeMillis());
                mongoTemplate.updateFirst(query,update,UserLike.class);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }




    /**
     * 查询互相喜欢人的所有id
     * @param page
     * @param pagesize

     * @param userId
     * @return
     */
    @Override
    public List<UserLike> geteachLoveCount(int page, int pagesize,  Long userId) {
        //获取到我喜欢的人的id
        Query query=Query.query(Criteria.where("userId").is(userId).and("isLike").is(true));
        List<UserLike> userLikes = mongoTemplate.find(query, UserLike.class);
        //获取到我喜欢的人的id
        List<Long> likeUserId = CollUtil.getFieldValues(userLikes, "likeUserId", Long.class);
        //查询我喜欢人的id是同时喜欢我的
        Query query1 = Query.query(Criteria.where("likeUserId").is(userId).and("isLike").is(true).and("userId").in(likeUserId))
                .limit(pagesize).skip((page-1)*pagesize).with(Sort.by(Sort.Order.desc("updated")));
        List<UserLike> userLikes1 = mongoTemplate.find(query1, UserLike.class);
        return userLikes1;
    }

    /**
     * 查询我喜欢的人
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public List<UserLike> getloveCount(int page, int pagesize, Long userId) {
        //获取到我喜欢的人的id
        Query query=Query.query(Criteria.where("userId").is(userId).and("isLike").is(true))
                .limit(pagesize).skip((page-1)*pagesize).with(Sort.by(Sort.Order.desc("updated")));
        List<UserLike> userLikes = mongoTemplate.find(query, UserLike.class);
        return userLikes;
    }


    /**
     * 喜欢我的用户
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public List<UserLike> getfanCount(int page, int pagesize, Long userId) {
        //获取到喜欢我的人的id
        Query query=Query.query(Criteria.where("likeUserId").is(userId).and("isLike").is(true))
                .limit(pagesize).skip((page-1)*pagesize).with(Sort.by(Sort.Order.desc("updated")));;
        List<UserLike> userLikes = mongoTemplate.find(query, UserLike.class);
        return userLikes;
    }

    /**
     * 取消喜欢
     * @param userId 当前用户id
     * @param uid 取消喜欢的用户id
     */
    @Override
    public void CancelLike(Long userId, Long uid) {
        //判断MongoDB中是否存在
        Query query=Query.query(Criteria.where("userId").is(userId).and("likeUserId")
                .is(uid).and("isLike").is(true));
        UserLike userLike = mongoTemplate.findOne(query, UserLike.class);

        if(userLike!=null){
            mongoTemplate.remove(userLike);
        }

    }

    /**
     * 喜欢粉丝
     * @param userId 当前用户id
     * @param uid 喜欢的粉丝id
     */
    @Override
    public void addlove(Long userId, Long uid) {
        //判断MongoDB中是否存在
        Query query=Query.query(Criteria.where("userId").is(userId).and("likeUserId")
                .is(uid).and("isLike").is(true));
        UserLike userLike = mongoTemplate.findOne(query, UserLike.class);

        if(userLike == null){
            userLike=new UserLike();
            userLike.setUserId(userId);
            userLike.setIsLike(true);
            userLike.setLikeUserId(uid);
            userLike.setCreated(System.currentTimeMillis());
            userLike.setUpdated(System.currentTimeMillis());
            mongoTemplate.save(userLike);
        }
    }

    /**
     * 用户互相喜欢的数量
     * @param userId 当前用户id
     * @return
     */
    @Override
    public long CountEachLoveCount(Long userId) {
        //获取到我喜欢的人的id
        Query query=Query.query(Criteria.where("userId").is(userId).and("isLike").is(true));
        List<UserLike> userLikes = mongoTemplate.find(query, UserLike.class);

        //获取到我喜欢的人的id
        List<Long> likeUserId = CollUtil.getFieldValues(userLikes, "likeUserId", Long.class);
        //查询我喜欢人的id是同时喜欢我的
        Query query1 = Query.query(Criteria.where("likeUserId").is(userId).and("isLike").is(true).and("userId").in(likeUserId));
        long count = mongoTemplate.count(query1, UserLike.class);

        return count;
    }

    /**
     * 当前用户喜欢的数量
     * @param userId
     * @return
     */
    @Override
    public long CountLoveCount(Long userId) {
        //获取到我喜欢的人的id
        Query query=Query.query(Criteria.where("userId").is(userId).and("isLike").is(true));
        long count = mongoTemplate.count(query, UserLike.class);
        return count;
    }

    /**
     * 喜欢当前用户的粉丝数
     * @param userId
     * @return
     */
    @Override
    public long CountFanCount(Long userId) {
        //统计喜欢当前用户的数量
        Query query=Query.query(Criteria.where("isLike").is(true).and("LikeUserId").is(userId));
        long count = mongoTemplate.count(query, UserLike.class);
        return count;
    }

    /**
     * 判断我是否喜欢这个人
     * @param userId 当前用户id
     * @param likeUserId 喜欢人的id
     * @return
     */
    @Override
    public boolean islike(Long userId, Long likeUserId) {
        Query query=Query.query(Criteria.where("isLike").is(true).and("LikeUserId").is(likeUserId).and("userId").is(userId));
        //判断是否存在
        boolean exists = mongoTemplate.exists(query, UserLike.class);
        return exists;
    }


}
