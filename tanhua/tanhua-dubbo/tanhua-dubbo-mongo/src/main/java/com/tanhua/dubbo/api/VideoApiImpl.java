package com.tanhua.dubbo.api;

import api.VideoApi;
import com.tanhua.dubbo.utils.IdWorker;
import mongo.Video;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import vo.PageResult;

import java.util.List;

@DubboService
public class VideoApiImpl implements VideoApi {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IdWorker idWorker;
    /**
     * 上传视频
     * @param video
     */
    @Override
    public String saveVideos(Video video) {
        //主键自动增长
        video.setVid(idWorker.getNextId("video"));
        //创建时间
        video.setCreated(System.currentTimeMillis());
        //进行保存
        Video save = mongoTemplate.save(video);
        String id =  save.getId().toHexString();
        return id;
    }

    /**
     * 根据vid查询数据列表
     * @param vids
     * @return
     */
    @Override
    public List<Video> findMovementByids(List<Long> vids) {
        Query query=Query.query(Criteria.where("vid").in(vids));
        return mongoTemplate.find(query,Video.class);
    }

    /**
     * 分页查询数据
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public List<Video> queryVideoList(int page, Integer pagesize) {
        Query query=new Query().limit(pagesize).skip((page-1)*pagesize)
                        .with(Sort.by(Sort.Order.desc("created")));

        return mongoTemplate.find(query,Video.class);
    }

    /**
     * 根据视频id查询发布者
     * @param id
     * @return
     */
    @Override
    public Video findMovementByid(String id) {
        Query query=Query.query(Criteria.where("_id").is(new ObjectId(id)));
      return   mongoTemplate.findOne(query,Video.class);
    }

    /**
     * 查询作者发布的视频
     * @param page
     * @param pagesize
     * @param uid
     * @return
     */
    @Override
    public PageResult findByUserId(Integer page, Integer pagesize, Long uid) {
    //根据出入的用户id,分页查询发布的视频
        Query query=Query.query(Criteria.where("userId").is(uid));
        //查询总条数
        long count = mongoTemplate.count(query, Video.class);
        //分页查询
        query.limit(pagesize).skip((page-1)*pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        List<Video> list = mongoTemplate.find(query, Video.class);

        return new PageResult(page,pagesize,(int)count,list);
    }

}
