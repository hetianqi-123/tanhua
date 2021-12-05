package com.tanhua.dubbo.api;

import api.peachblossomApi;
import cn.hutool.core.util.RandomUtil;
import mongo.Voice;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;


@DubboService
public class peachblossomApiImpl implements peachblossomApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 桃花传音-发送语音
     * @param voice
     * @return
     */
    @Override
    public String saveVoice(Voice voice) {
        Voice save = mongoTemplate.save(voice);
     return save.getId().toHexString();
    }

    @Override
    public Voice suiji(Long userId) {
        Query query=Query.query(Criteria.where("userId").ne(userId));
        //统计有多少条
        long count = mongoTemplate.count(query, Voice.class);
        //把统计的数量当做索引,去随机获取一条
        int l = RandomUtil.randomInt(0, (int) count);
        //根据分页查询随机数的语音
        //每页显示一条,从l-1开始获取
        query.limit(1).skip(l-1);
        Voice one = mongoTemplate.findOne(query, Voice.class);
        return one;
    }

    /**
     * 删除语音
     * @param voice
     */
    @Override
    public void deleteyuyin(Voice voice) {
        if(voice!=null){
            Query query = new Query(Criteria.where("_id").is(voice.getId()));
            mongoTemplate.remove(query,Voice.class);
        }

    }


}
