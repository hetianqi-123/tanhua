package com.tanhua.dubbo.api;

import api.CommentApi;
import api.MovementApi;
import enums.CommentType;
import mongo.Comment;
import mongo.Movement;
import mongo.Video;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@DubboService
public class CommentApiImpl implements CommentApi {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MovementApi movementApi;

    /**
     * 发布评论
     * @param com
     * @return
     */
    @Override
    public Integer save(Comment com) {
        //根据动态的id查询发布动态的用户id
        Movement movement = mongoTemplate.findById(com.getPublishId(), Movement.class);
        //判断获取的是否为null
        if(movement!=null){
            //将发布动态的用户id保存到Comment封装类
            com.setPublishUserId(movement.getUserId());
        }
        //Comment保存到数据库
        mongoTemplate.save(com);
        Query query = Query.query(Criteria.where("id").is(com.getPublishId()));
        Update update=new Update();
        //判断当前用户是哪项操作,然后累加为1
        if(com.getCommentType() == CommentType.LIKE.getType()) {
            update.inc("likeCount",1);
        }else if (com.getCommentType() == CommentType.COMMENT.getType()){
            update.inc("commentCount",1);
        }else {
            update.inc("loveCount",1);
        }
        //设置更新参数
        FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions();
        findAndModifyOptions.returnNew(true) ;//获取更新后的最新数据
        //现在封装类里面就有更新之后的所有数据
        Movement modify = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Movement.class);
        //把操作类型返回过去
        return modify.statisCount(com.getCommentType());
    }

    /**
     * 查询发布列表
     * @param movementId 动态编号
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public List<Comment> findComments(String movementId, int page, int pagesize,CommentType comment) {
        //根据动态编号和类型去查询当前所有的评论人
        Criteria criteria=Criteria.where("publishId").in(new ObjectId(movementId)).and("commentType")
                .is(comment.getType());
        Query query=Query.query(criteria).skip((page-1)*pagesize).limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        //获取到当前动态的所有评论
        return mongoTemplate.find(query, Comment.class);
    }


    /**
     * 判断当前数据是否存在
     * @param publishId 发布id
     * @param userId 评论人
     * @param like 操作类型
     * @return
     */
    @Override
    public boolean islike(String publishId, Long userId, CommentType like) {
        Criteria criteria=Criteria.where("publishId").is(new ObjectId(publishId)).and("userId")
                .is(userId).and("commentType").is(like.getType());
        Query query=Query.query(criteria);
        //判断当前数据是否存在
        return  mongoTemplate.exists(query, Comment.class);
    }


    /**
     * 删除点赞数据
     * @param comment
     * @return
     */
    @Override
    public Integer delete(Comment comment) {
        //移除Comment表中的此条点赞数据
        Criteria criteria=Criteria.where("publishId").is(comment.getPublishId()).and("commentType").is(comment.getCommentType())
                                    .and("userId").is(comment.getUserId());
        Query query=Query.query(criteria);
        mongoTemplate.remove(query,Comment.class);
        //2、修改动态表中的总数量
        Query movementQuery = Query.query(Criteria.where("id").is(comment.getPublishId()));
        Update update = new Update();

        //判断当前用户是哪项操作,然后累加为1
        if(comment.getCommentType() == CommentType.LIKE.getType()) {
            update.inc("likeCount",-1);
        }else if (comment.getCommentType() == CommentType.COMMENT.getType()){
            update.inc("commentCount",-1);
        }else {
            update.inc("loveCount",-1);
        }
        //设置更新参数
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true) ;//获取更新后的最新数据
        Movement modify = mongoTemplate.findAndModify(movementQuery, update, options, Movement.class);
        //5、获取最新的评论数量，并返回
        return modify.statisCount(comment.getCommentType());
    }

    /**
     * 对评论进行点赞
     * @param id 评论id
     * @param userId 用户id
     * @param like 操作类型
     * @return
     */
    @Override
    public Comment pldz(String id, Long userId, CommentType like) {
        Criteria criteria=Criteria.where("_id").is(new ObjectId(id));
        Query query=Query.query(criteria);
        //查询全部,拿到发布id
        Comment one = mongoTemplate.findOne(query, Comment.class);
        System.out.println(one);
        return  one;

//        //判断当前数据是否存在
//        return  mongoTemplate.exists(query, Comment.class);
    }

    /**
     * 保存评论点赞数据
     * @param comment
     * @return
     */
    @Override
    public Integer savepldz(Comment comment) {
        System.out.println(comment);

        //将点赞的数据保存到数据库
        Comment comm = mongoTemplate.save(comment);
        //进行修改点赞数
        Query query = Query.query(Criteria.where("_id").is(comm.getId()));
        Update update=new Update();
        //if(comment.getCommentType() == CommentType.LIKE.getType()) {
            update.inc("likeCount", 1);
        //}
        //设置更新参数
        FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions();
        findAndModifyOptions.returnNew(true) ;//获取更新后的最新数据
        //现在封装类里面就有更新之后的所有数据
        Comment andModify = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Comment.class);
        //把数量返回回去
        return andModify.getLikeCount();
    }

    /**
     * 删除评论点赞
     * @param id
     * @return
     */
    @Override
    public Integer deletepldz(String  id) {
        //进行点赞数量更新减一
        Query query = Query.query(Criteria.where("id").is(new ObjectId(id)));
        Update update=new Update();
        //判断当前用户是哪项操作,然后累加为1
        //if(comment.getCommentType() == CommentType.LIKE.getType()) {
            update.inc("likeCount",-1);
        //}
        //设置更新参数
        FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions();
        findAndModifyOptions.returnNew(true) ;//获取更新后的最新数据
        //现在封装类里面就有更新之后的所有数据
        Comment andModify = mongoTemplate.findAndModify(query, update, findAndModifyOptions, Comment.class);
        //把数量返回回去
        return andModify.getLikeCount();
    }

    /**
     * 查询点赞数
     * @param page
     * @param pagesize
     * @param publishUserId
     * @param
     * @return
     */
    @Override
    public List<Comment> findlike(Integer page, Integer pagesize, Long publishUserId,  CommentType like) {
       Criteria criteria=Criteria.where("publishUserId").is(publishUserId).and("commentType")
               .is(like.getType());
       Query query=Query.query(criteria).skip((page-1)*pagesize).limit(pagesize)
               .with(Sort.by(Sort.Order.desc("created")));
        //获取到当前用户发送动态的所有的点赞用户
       return mongoTemplate.find(query, Comment.class);
    }

    /**
     * 保存视频点赞
     * @param comment
     */
    @Override
    public void savevideo(Comment comment) {
        //把点赞数据保存到comment表中
      mongoTemplate.save(comment);
      //保存到动态表之后,在Video表中进行查询
        //视频表的id和动态表的id相等
        System.out.println(comment.getPublishId());
        System.out.println(comment.getCommentType());
        System.out.println(CommentType.LIKE.getType());
        System.out.println(CommentType.LIKE);


        Query query = Query.query(Criteria.where("_id").is(comment.getPublishId()));
      //对视频表的点赞数进行加1
        Update update=new Update();
        if(comment.getCommentType()==CommentType.LIKE.getType()){
            update.inc("likeCount",1);
        }else if(comment.getCommentType()==CommentType.COMMENT.getType()){
            update.inc("commentCount",1);
        }else if(comment.getCommentType()==CommentType.LOVE.getType()){
            update.inc("loveCount",1);
        }
        //设置更新参数
        FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions();
        findAndModifyOptions.returnNew(true) ;//获取更新后的最新数据
        //现在封装类里面就有更新之后的所有数据
        mongoTemplate.findAndModify(query, update, findAndModifyOptions,Video.class);
    }

    /**
     * 删除视频点赞
     * @param comment
     */
    @Override
    public void deletevideo(Comment comment) {
       //首先从动态表把当前点赞删除
        Criteria criteria=Criteria.where("publishId").is(comment.getPublishId()).and("userId").is(comment.getUserId());
        Query query=Query.query(criteria);
        //删除动态表中的点赞记录
        mongoTemplate.remove(query,Comment.class);
        System.out.println(comment.getCommentType());
        System.out.println(CommentType.LIKE.getType());
        Query query2 = Query.query(Criteria.where("id").is(comment.getPublishId()));
        //视频表中的点赞-1
        Update update=new Update();
        if(comment.getCommentType()==CommentType.LIKE.getType()){
            update.inc("likeCount",-1);
        }else if(comment.getCommentType()==CommentType.COMMENT.getType()){
            update.inc("commentCount",-1);
        }else if(comment.getCommentType()==CommentType.LOVE.getType()){
            update.inc("loveCount",-1);
        }
        //设置更新参数
        FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions();
        findAndModifyOptions.returnNew(true) ;//获取更新后的最新数据
        //现在封装类里面就有更新之后的所有数据
        mongoTemplate.findAndModify(query2, update, findAndModifyOptions,Video.class);
    }

    /**
     * 保存视频评论
     * @param comment
     */
    @Override
    public void saveshipingcomments(Comment comment) {
        //Comment保存到数据库
        mongoTemplate.save(comment);
        //视频表的评论数+1
        Query query = Query.query(Criteria.where("_id").is(comment.getPublishId()));
        Update update=new Update();
        //判断当前用户是哪项操作,然后累加为1
        if(comment.getCommentType() == CommentType.LIKE.getType()) {
            update.inc("likeCount",1);
        }else if (comment.getCommentType() == CommentType.COMMENT.getType()){
            update.inc("commentCount",1);
        }else {
            update.inc("loveCount",1);
        }
        //设置更新参数
        FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions();
        findAndModifyOptions.returnNew(true) ;//获取更新后的最新数据
        //现在封装类里面就有更新之后的所有数据
        mongoTemplate.findAndModify(query, update, findAndModifyOptions, Video.class);
    }


}
