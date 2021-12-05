package com.tanhua.dubbo.api;

import api.MovementApi;
import cn.hutool.core.collection.CollUtil;
import com.tanhua.dubbo.utils.IdWorker;
import com.tanhua.dubbo.utils.timelineService;
import mongo.Movement;
import mongo.MovementTimeLine;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import vo.PageResult;

import java.util.List;

@DubboService
public class MovementApiImpl implements MovementApi {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IdWorker itworker;

    @Autowired
    private timelineService timelineService;

    /**
     * 发布动态
     * @param movement
     */
    @Override
    public String movements(Movement movement) {
        try {
            //完善movement封装类的数据
            //设置pid,保持自增
            movement.setPid(itworker.getNextId("movement"));
            System.out.println(movement.getPid()+"自动生成的id");
            //添加发布时间Long类型的,所以是时分秒
            movement.setCreated(System.currentTimeMillis());
            //将封装类保存到数据库
            mongoTemplate.save(movement);

            timelineService.saveTimeLine(movement.getUserId(),movement.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return movement.getId().toHexString();

    }

    /**
     * 查询个人用户的动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult findById(int page, int pagesize, Long userId) {
        /**
         * 查询当前用户的所有动态,按照时间倒序分页
         */
        Criteria criteria = Criteria.where("userId").is(userId).and("state").is(1);
        Query query = Query.query(criteria).skip((page -1 ) * pagesize).limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        List<Movement> movements = mongoTemplate.find(query, Movement.class);
        return new PageResult(page,pagesize,0,movements);
    }

    /**
     * 查询好友动态
     * @param friendId 当前用户的id
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public List<Movement> findFriendMovements(Long friendId, int page, int pagesize) {
        //根据当前用户id查询动态时间线表,获取到多个好友的动态id
        Query query = Query.query(Criteria.where("friendId").in(friendId))
                .skip((page - 1)*pagesize).limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        //获取到时间线表的当前用户的时间线表的数据
        List<MovementTimeLine> movementTimeLines = mongoTemplate.find(query, MovementTimeLine.class);
        //获取到时间线表的当前用户的所有好友的动态id
        List<ObjectId> movementId = CollUtil.getFieldValues(movementTimeLines, "movementId", ObjectId.class);
        //根据当前好友的动态id集合去查询动态表,获取到好友的所有的动态
        Query query1= Query.query(Criteria.where("id").in(movementId).and("state").is(1)).with(Sort.by(Sort.Order.desc("created")));
        List<Movement> movements = mongoTemplate.find(query1, Movement.class);
        return movements;
    }

    /**
     * 动态推荐好友---推荐的有pid
     * @param pids
     * @return
     */
    @Override
    public List<Movement> findByPids(List<Long> pids) {
        //根据pid查询动态
        //因为前面进行了内存分页,所以不需要再分页了
        Criteria criteria=Criteria.where("pid").in(pids);
        Query query=Query.query(criteria);
        //查询多个推荐的好友动态
        List<Movement> movements = mongoTemplate.find(query, Movement.class);
        return movements;
    }

    /**
     * 动态推荐好友---推荐的没有pid
     * @param pagesize
     * @return
     */
    @Override
    public List<Movement> randomMovements(int pagesize) {
        //随机生成5个Movement表中的动态数据  第一次传入Movement.class为了解析操作的数据库表
        TypedAggregation aggregation = Aggregation.newAggregation(Movement.class,
                Aggregation.sample(pagesize));
        //设置好之后调用mongoTemplate.aggregate()方法进行执行
        // 第二次传入Movement.class是为了将结果设置到是为了对象
        AggregationResults<Movement> movements = mongoTemplate.aggregate(aggregation,Movement.class);

        return movements.getMappedResults();
    }

    /**
     * 查询单条动态
     * @param movementId
     * @return
     */
    @Override
    public Movement findMovementById(String movementId) {
        //根据动态id获取到当前的动态详情
        return mongoTemplate.findById(movementId,Movement.class);
    }

    /**
     * 查询用户的动态
     * @param uid
     * @param state
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public PageResult findByUserId(Long uid, Integer state, Integer page, Integer pagesize) {
        //先进行分页查询
        Query query = new Query().skip((page - 1) * pagesize).limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        //因为是两用的,判断当前用户有没有传id
        if(uid!=null){
            query.addCriteria(Criteria.where("userId").is(uid));
        }
        if(state!=null){
            query.addCriteria(Criteria.where("state").is(state));
        }
        List<Movement> list = mongoTemplate.find(query, Movement.class);
        long count = mongoTemplate.count(query, Movement.class);
        return new PageResult(page,pagesize,(int)count,list);
    }

    /**
     * 修改审核状态
     * @param movementId
     * @param state
     */
    @Override
    public void update(String movementId, int state) {
        //先查询出来,再更新
        Query query=Query.query(Criteria.where("id").is(new ObjectId(movementId)));
        Update update = Update.update("state",state);
        //判断是否更新成功
        mongoTemplate.updateFirst(query,update,Movement.class);
    }


}
