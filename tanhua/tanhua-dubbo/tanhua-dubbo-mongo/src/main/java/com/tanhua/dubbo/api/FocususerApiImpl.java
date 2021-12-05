package com.tanhua.dubbo.api;

import api.FocususerApi;
import mongo.Focususer;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@DubboService
public class FocususerApiImpl implements FocususerApi {

    @Autowired
    private MongoTemplate mongoTemplate;
    /**
     * 关注视频作者
     * @param focususer
     * @return
     */
    @Override
    public String guanzhu(Focususer focususer) {
            Focususer save = mongoTemplate.save(focususer);
            return save.getId().toHexString();
    }

    /**
     * 取消关注
     * @param userId 当前用户id
     * @param uid 取消关注的用户id
     */
    @Override
    public void quxiaoguanzhu(Long userId, Long uid) {
        Query query=Query.query(Criteria.where("userId").is(userId).and("followUserId").is(uid));
        Focususer one = mongoTemplate.findOne(query, Focususer.class);
        if(one!=null){
            mongoTemplate.remove(query,Focususer.class);
        }
    }
}
