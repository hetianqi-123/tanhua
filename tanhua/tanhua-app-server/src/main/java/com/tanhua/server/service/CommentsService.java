package com.tanhua.server.service;

import api.AnnouncementsApi;
import api.CommentApi;
import api.UserInfoApi;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import domain.Announcements;
import domain.AnnouncementsVo;
import domain.UserInfo;
import enums.CommentType;
import lombok.extern.slf4j.Slf4j;
import mongo.Comment;
import org.apache.dubbo.config.annotation.DubboReference;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import utils.Constants;
import vo.CommentDZVo;
import vo.CommentVo;
import vo.ErrorResult;
import vo.PageResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
//日志打印
@Slf4j
public class CommentsService {
    @DubboReference
    private AnnouncementsApi announcementsApi;
    @DubboReference
    private CommentApi commentApi;
    @DubboReference
    private UserInfoApi userInfoApi;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    /**
     * 发布评论
     * @param movementId 动态编号
     * @param comment 评论
     */
    public void saveComments(String movementId, String comment) {
        //发布评论的id,就是当前登录的用户id
        Long userId = UserHolder.getUserId();
        //把评论的信息封装到Comment封装类里面',然后保存到数据库即可
        Comment com=new Comment();
        //保存发布评论的动态id
        com.setPublishId(new ObjectId(movementId));
        //评论类型
        com.setCommentType(CommentType.COMMENT.getType());
        //发布的内容
        com.setContent(comment);
        //评论人
        com.setUserId(userId);
        //发表时间
        com.setCreated(System.currentTimeMillis());
        //保存到数据库,返回操作的类型
        Integer commentCount = commentApi.save(com);
        log.info("commentCount = " + commentCount);

    }

    /**
     * 评论列表
     * @param movementId 动态编号
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult findComments(String movementId,int page, int pagesize) {
        //根据动态编号参数查询到当前动态的所有评论
        List<Comment> comments = commentApi.findComments(movementId,page,pagesize,CommentType.COMMENT );
        //判断评论是否为null
        if(CollUtil.isEmpty(comments)){
            return new PageResult();
        }
        //不为null获取到所有的评论者的id
        List<Long> userId = CollUtil.getFieldValues(comments, "userId", Long.class);
        //根据id去查询所有评论者的详细信息
        Map<Long, UserInfo> longUserInfoMap = userInfoApi.finByIds(userId, null);
        //创建CommentVo集合
        List<CommentVo> listvo=new ArrayList<>();
        //合并到CommentVo中
        for (Comment comment : comments) {
        //每个评论人对应一个详细信息
            UserInfo userInfo = longUserInfoMap.get(comment.getUserId());
            if(userInfo!=null){
                CommentVo commentVo = CommentVo.init(userInfo, comment);
                //修复点赞的bug
                String key = Constants.MOVEMENTS_INTERACT_KEY + comment.getId().toHexString();
                String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();
                //如果获取到key,修改点赞状态为1
                if(redisTemplate.opsForHash().hasKey(key,hashKey)){
                    commentVo.setHasLiked(1);
                }
                //把多个commentvo保存到集合中
                listvo.add(commentVo);
            }
        }
       return new PageResult(page,pagesize,0,listvo);
    }

    /**
     * 评论点赞
     * @param id 评论id
     * @return
     */
    public Integer pldz(String id) {
        //获取到当前评论用户id
        Long userId = UserHolder.getUserId();
        //判断当前评论是否存在
        Comment pldz = commentApi.pldz(id, userId, CommentType.LIKE);
        //如果评论不存在
        if(pldz==null){
            throw  new BusinessException(ErrorResult.likeError());
        }
        //没有点赞过的话把数据封装下
        Comment comment=new Comment();
        //评论id
        comment.setId(new ObjectId(id));
       //评论点赞类型
        comment.setCommentType(pldz.getCommentType());
        //评论人
        comment.setUserId(userId);
        //评论时间
        comment.setCreated(System.currentTimeMillis());
        //评论内容
        comment.setContent(pldz.getContent());
        //获取到发布动态id
        comment.setPublishId(pldz.getPublishId());
        //被评论人的id
        comment.setPublishUserId(pldz.getPublishUserId());
        if(comment.getLikeCount()==null){
            comment.setLikeCount(0);
        }else {
            comment.setLikeCount(pldz.getLikeCount());
        }
        System.out.println(comment.getLikeCount());

        //进行保存,返回点赞数
        Integer count = commentApi.savepldz(comment);
        //同时加入到redis中
        String key = Constants.MOVEMENTS_INTERACT_KEY + id;
        String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();
        redisTemplate.opsForHash().put(key,hashKey,"1");
        return count;
    }


    /**
     * 评论取消点赞
     * @param id 评论id
     * @return
     */
    public Integer dislikepldz(String id) {
        //获取到当前评论用户id
        Long userId = UserHolder.getUserId();
        //判断当前评论是否存在
        Comment pldz = commentApi.pldz(id, userId, CommentType.LIKE);
        //如果评论不存在
        if(pldz==null){
            throw  new BusinessException(ErrorResult.likeError());
        }
        //进行保存,返回点赞数
        Integer count = commentApi.deletepldz(id);
        //进行对redis的删除
        String key = Constants.MOVEMENTS_INTERACT_KEY + id;
        String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();
        redisTemplate.opsForHash().delete(key,hashKey,"1");
        return count;

    }

    //喜欢
    public Integer loveComment(String movementId) {
        //1、调用API查询用户是否已点赞
        Boolean hasComment = commentApi.islike(movementId,UserHolder.getUserId(),CommentType.LOVE);
        //2、如果已经喜欢，抛出异常
        if(hasComment) {
            throw  new BusinessException(ErrorResult.loveError());
        }
        //3、调用API保存数据到Mongodb
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setCommentType(CommentType.LOVE.getType());
        comment.setUserId(UserHolder.getUserId());
        comment.setCreated(System.currentTimeMillis());
        Integer count = commentApi.save(comment);
        //4、拼接redis的key，将用户的点赞状态存入redis
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hashKey = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getUserId();
        redisTemplate.opsForHash().put(key,hashKey,"1");
        return count;
    }

    //取消喜欢
    public Integer disloveComment(String movementId) {
        //1、调用API查询用户是否已点赞
        Boolean hasComment = commentApi.islike(movementId,UserHolder.getUserId(),CommentType.LOVE);
        //2、如果未点赞，抛出异常
        if(!hasComment) {
            throw new BusinessException(ErrorResult.disloveError());
        }
        //3、调用API，删除数据，返回点赞数量
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setCommentType(CommentType.LOVE.getType());
        comment.setUserId(UserHolder.getUserId());
        Integer count = commentApi.delete(comment);
        //4、拼接redis的key，删除点赞状态
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hashKey = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getUserId();
        redisTemplate.opsForHash().delete(key,hashKey);
        return count;
    }

    /**
     * 点赞列表
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult findlike(Integer page, Integer pagesize) {
        //查询当前操作者发布的动态
      List<Comment> list= commentApi.findlike(page,pagesize,UserHolder.getUserId(),CommentType.LIKE);

        if(CollUtil.isEmpty(list)){
            return new PageResult();
        }
        //获取到点赞人的id
        List<Long> userId = CollUtil.getFieldValues(list, "userId", Long.class);
        //查询点赞人的详细信息
        Map<Long, UserInfo> longUserInfoMap = userInfoApi.finByIds(userId, null);
        //收集点赞信息
        List<CommentDZVo> vo=new ArrayList<>();
        for (Comment comment : list) {
            //获取到详细信息单个点赞人的详细信息
            UserInfo userInfo = longUserInfoMap.get(comment.getUserId());
            if(userInfo!=null){
                CommentDZVo commentDZVo = CommentDZVo.init(userInfo, comment);
                vo.add(commentDZVo);
            }

        }
    return new PageResult(page,pagesize,0,vo);

    }

    /**
     * 评论列表
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult findcomments(Integer page, Integer pagesize) {
        //查询当前操作者发布的动态
        List<Comment> list = commentApi.findlike(page, pagesize, UserHolder.getUserId(), CommentType.COMMENT);

        if (CollUtil.isEmpty(list)) {
            return new PageResult();
        }
        //获取到点赞人的id
        List<Long> userId = CollUtil.getFieldValues(list, "userId", Long.class);
        //查询点赞人的详细信息
        Map<Long, UserInfo> longUserInfoMap = userInfoApi.finByIds(userId, null);
        //收集点赞信息
        List<CommentDZVo> vo = new ArrayList<>();
        for (Comment comment : list) {
            //获取到详细信息单个点赞人的详细信息
            UserInfo userInfo = longUserInfoMap.get(comment.getUserId());
            if (userInfo != null) {
                CommentDZVo commentDZVo = CommentDZVo.init(userInfo, comment);
                vo.add(commentDZVo);
            }
        }
        return new PageResult(page, pagesize, 0, vo);
    }


    /**
     * 喜欢列表
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult findloves(Integer page, Integer pagesize) {
        //查询当前操作者发布的动态
        List<Comment> list = commentApi.findlike(page, pagesize, UserHolder.getUserId(), CommentType.LOVE);

        if (CollUtil.isEmpty(list)) {
            return new PageResult();
        }
        //获取到点赞人的id
        List<Long> userId = CollUtil.getFieldValues(list, "userId", Long.class);
        //查询点赞人的详细信息
        Map<Long, UserInfo> longUserInfoMap = userInfoApi.finByIds(userId, null);
        //收集点赞信息
        List<CommentDZVo> vo = new ArrayList<>();
        for (Comment comment : list) {
            //获取到详细信息单个点赞人的详细信息
            UserInfo userInfo = longUserInfoMap.get(comment.getUserId());
            if (userInfo != null) {
                CommentDZVo commentDZVo = CommentDZVo.init(userInfo, comment);
                vo.add(commentDZVo);
            }
        }
        return new PageResult(page, pagesize, 0, vo);
    }

    /**
     * 公告列表
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult findannouncements(Integer page, Integer pagesize) {
        //查询出所有的公告,设置分页
        IPage<Announcements> iPage= announcementsApi.findannouncements(page,pagesize);
        List<Announcements> records = iPage.getRecords();
        List<AnnouncementsVo> volist=new ArrayList<>();
        for (Announcements record : records) {
            AnnouncementsVo vo=new AnnouncementsVo();
            BeanUtils.copyProperties(record, vo);
                vo.setCreateDate(record.getCreated());
            volist.add(vo);
        }

        PageResult pr = new PageResult(page,pagesize,(int)iPage.getTotal(),volist);
        //返回
        return pr;

        //return new PageResult(page,pagesize,0,list);




    }
}
