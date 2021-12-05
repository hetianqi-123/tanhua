package com.tanhua.dubbo.api;

import api.visitorsApi;
import mongo.Visitors;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class visitorsApiImpl  implements visitorsApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存访客信息
     * @param visitors
     */
    @Override
    public void save(Visitors visitors) {
        //一天只能保存一个访客信息
        //所以先进行查询,是否已经保存了访客信息
        Query query = Query.query(Criteria.where("userId").is(visitors.getUserId()).and("visitorUserId").is(visitors.getVisitorUserId())
                .and("visitDate").is(visitors.getVisitDate()));
        //判断是否存在
        if(!mongoTemplate.exists(query,Visitors.class)){
            mongoTemplate.save(visitors);
        }

    }

    /**
     * 查询首页的访客
     * @param date 最后一次查询的时间
     * @param userId 当前用户的id
     * @return
     */
    @Override
    public List<Visitors> queryMyVisitors(Long date, Long userId) {
        //根据用户查询访问数据库
        Criteria criteria=Criteria.where("userId").is(userId);
        //如果上次查询时间不是空的话
        if(date!=null){
            //这次的查询时间要大于上次的时间
            criteria.and("date").gt(date);
        }
        //并且展示最新的5条,所以要排序和分页
        Query query= Query.query(criteria).limit(5).with(Sort.by(Sort.Order.desc("date")));
        //返回查询的数据
        List<Visitors> list = mongoTemplate.find(query, Visitors.class);
        return list;
    }
}
