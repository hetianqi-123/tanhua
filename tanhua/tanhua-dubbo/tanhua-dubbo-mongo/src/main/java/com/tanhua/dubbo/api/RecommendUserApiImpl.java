package com.tanhua.dubbo.api;

import api.RecommendUserApi;
import cn.hutool.core.collection.CollUtil;
import mongo.RecommendUser;
import mongo.UserLike;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import vo.PageResult;

import java.util.List;
import java.util.Map;

@DubboService
public class RecommendUserApiImpl  implements RecommendUserApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 查询今日佳人
     * @param toUserId 当前登录的用户id
     * @return
     */
    public RecommendUser queryWithMaxScore(Long toUserId) {

        //根据toUserId查询，根据评分score排序，获取第一条
        //构建Criteria
        //先查询当前用户的id
        Criteria criteria=Criteria.where("toUserId").is(toUserId);
        //构建query对象
        //把查询条件输入之后根据score分数进行倒序,并且利用分页只显示一条
        Query query=Query.query(criteria).with(Sort.by(Sort.Order.desc("score")))
                .limit(1);
        //调用mongoTemplate查询出这条数据并进行返回
        return mongoTemplate.findOne(query,RecommendUser.class);
    }

    /**
     * 根据当前用户id进行分页好友查询
     * @param page
     * @param pagesize
     * @param toUserId 当前登录的用户id
     * @return
     */
    @Override
    public PageResult recommendationList(Integer page, Integer pagesize, Long toUserId) {
        //1、构建Criteria对象
        Criteria criteria = Criteria.where("toUserId").is(toUserId);
        //2、创建Query对象
        Query query = Query.query(criteria).with(Sort.by(Sort.Order.desc("score"))).limit(pagesize)
                .skip((page - 1) * pagesize);
        //3、调用mongoTemplate查询
        List<RecommendUser> list = mongoTemplate.find(query, RecommendUser.class);
        long count = mongoTemplate.count(query, RecommendUser.class);
        //4、构建返回值PageResult
        return  new PageResult(page,pagesize,(int)count,list);
    }

    /**
     * 根据当前用户id和佳人id查询佳人信息
     * @param userId 佳人id
     * @param touserId 当前用户id
     * @return
     */
    @Override
    public RecommendUser queryByUserId(Long userId, Long touserId) {
       Criteria criteria=Criteria.where("userId").is(userId).and("toUserId").is(touserId);
       Query query=Query.query(criteria);
        RecommendUser user = mongoTemplate.findOne(query, RecommendUser.class);
        //当user为null的时候,自己构建默认数据进去,保证有数据
        if(user == null) {
            user = new RecommendUser();
            user.setUserId(userId);
            user.setToUserId(touserId);
            //构建缘分值
            user.setScore(95d);
        }
        return user;
    }

    /**
     * 左滑右滑,排除不喜欢,或者新欢的好友
     * @param userId 当前用户id,查询数量
     * @param counts
     * @return
     */
    @Override
    public List<RecommendUser> islike(Long userId, int counts) {
        //1、查询喜欢不喜欢的用户ID
        List<UserLike> likeList = mongoTemplate.find(Query.query(Criteria.where("userId").is(userId)), UserLike.class);
        List<Long> likeUserIdS = CollUtil.getFieldValues(likeList, "likeUserId", Long.class);
        //2、构造查询推荐用户的条件
        Criteria criteria = Criteria.where("toUserId").is(userId).and("userId").nin(likeUserIdS);
        //3、使用统计函数，随机获取推荐的用户列表
        TypedAggregation<RecommendUser> newAggregation = TypedAggregation.newAggregation(RecommendUser.class,
                Aggregation.match(criteria),//指定查询条件
                Aggregation.sample(counts)
        );
        AggregationResults<RecommendUser> results = mongoTemplate.aggregate(newAggregation, RecommendUser.class);
        //4、构造返回
        return results.getMappedResults();
    }

    /**
     * 查询互相喜欢好友的好感度
     * @param userIds 互相喜欢的好友id
     * @param userId 当前操作用户的id
     * @return
     */
    @Override
    public Map<Long, RecommendUser> finByIds(List<Long> userIds, Long userId) {
        //查询当前用户的与好友的互相喜欢人的好感度
    Query query=Query.query(Criteria.where("userId").is(userId).and("toUserId").in(userIds));
        List<RecommendUser> recommendUsers = mongoTemplate.find(query, RecommendUser.class);
        //转化为map
        Map<Long, RecommendUser> List = CollUtil.fieldValueMap(recommendUsers, "toUserId");
        return List;
    }


}