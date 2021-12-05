package com.tanhua.server.service;

import api.CommentApi;
import api.FocususerApi;
import api.UserInfoApi;
import api.VideoApi;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.PageUtil;
import com.aliyuncs.utils.StringUtils;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import domain.UserInfo;
import enums.CommentType;
import mongo.Comment;
import mongo.Focususer;
import mongo.Video;
import org.apache.dubbo.config.annotation.DubboReference;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import utils.Constants;
import vo.ErrorResult;
import vo.PageResult;
import vo.VideoVo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SmallVideosService {

    //上传视频对象
    @Autowired
    private FastFileStorageClient clientc;
    //访问地址
    @Autowired
    private FdfsWebServer webServer;
    //上传图片对象
    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @DubboReference
    private VideoApi videoApi;
    @DubboReference
    private UserInfoApi userInfoApi;
    @DubboReference
    private FocususerApi focususerApi;

    @Autowired
    private MqMessageService mqMessageService;


    /**
     * 上传视频
     * @param videoThumbnail 视频封面文件
     * @param videoFile 视频文件
     */
    public void saveVideos(MultipartFile videoThumbnail, MultipartFile videoFile) throws IOException {
        //如果视频或者封面是空白的,抛出异常
        if(videoFile.isEmpty()||videoThumbnail.isEmpty()){
            throw new BusinessException(ErrorResult.error());
        }
        //1.将视频上传到fastDFS,获取上传路径
        //获取到视频
        //获取视频的文件名称   abc.map4
        String originalFilename = videoFile.getOriginalFilename();
        //先获取最后一位.的索引+1,然后进行截取
        originalFilename= originalFilename.substring(originalFilename.lastIndexOf(".")+1);

        StorePath path = clientc.uploadFile(videoFile.getInputStream(), videoFile.getSize(), originalFilename, null);
        //获取到绝对路径
        String fullPath = path.getFullPath();
        //获取到Nginx反向代理的访问地址
        String videourl =webServer.getWebServerUrl()+fullPath;
        //将封面上传到oss中,获取上传路径
        String imgUrl = ossTemplate.upload(videoThumbnail.getOriginalFilename(), videoThumbnail.getInputStream());
        //构造Video封装对象
        Video video=new Video();
        //用户id
        video.setUserId(UserHolder.getUserId());
        //视频路径
        video.setVideoUrl(videourl);
        //封面路径
        video.setPicUrl(imgUrl);
        //文字描述
        video.setText("我就是我,不一样的烟火");
        //保存到MongoDB数据库中
        String videoid=videoApi.saveVideos(video);
        if(StringUtils.isEmpty(videoid)){
            throw new BusinessException(ErrorResult.error());
        }

        //发送消息
        mqMessageService.sendLogMessage(UserHolder.getUserId(),"0301","video",videoid);
    }

    /**
     * 视频列表
     * @param page 当前页
     * @param pagesize 每页显示的数据
     * @return
     */
    //自动配置缓存
    @Cacheable(value = "videos",key="T(com.tanhua.server.interceptor.UserHolder).getUserId()+'_'+#page+'_'+#pagesize") //userid_page_pagesize
    public PageResult queryVideoList(Integer page, Integer pagesize) {
        //查询redis中的数据
        //拼接redis
        String key= Constants.VIDEOS_RECOMMEND+UserHolder.getUserId();
        String value = redisTemplate.opsForValue().get(key);
        List<Video> list=new ArrayList<>();
        int redisPages=0;
        //判断redis中的数据是否存在
        if(!StringUtils.isEmpty(value)){
            //根据","进行分割
            String[] values= value.split(",");
            //判断当前的起始数是否小于数组总数
            if((page-1)*pagesize < values.length){
                //进行内存分页
                List<Long> vids = Arrays.stream(values).skip((page - 1) * pagesize).limit(pagesize)
                        //转换为Long类型
                        .map(e -> Long.valueOf(e))
                        .collect(Collectors.toList());
                //根据pid数据查询动态数据
               list= videoApi.findMovementByids(vids);
            }
            //自动算出redis多少页
            redisPages= PageUtil.totalPage(values.length,pagesize);
        }
        //如果redis数据不存在,分页查询视频数据
        if(list.isEmpty()){
            //page的计算规则,传入的页码,
            list=videoApi.queryVideoList(page-redisPages,pagesize);
        }
        //提取视频列表中的所有用户的id
        List<Long> userId = CollUtil.getFieldValues(list, "userId", Long.class);
        //查询用户的信息
        Map<Long, UserInfo> map = userInfoApi.finByIds(userId, null);
        //构建返回值
        List<VideoVo> vos=new ArrayList<>();
        for (Video video : list) {
            UserInfo userInfo = map.get(video.getUserId());
            if(userInfo!=null){
                VideoVo vo = VideoVo.init(userInfo, video);
                //判断redis中是否存在关注
                String key1 =Constants.FOCUS_USER+UserHolder.getUserId().toString();
                String value1=video.getUserId().toString();
                //判断是否点赞
                String key2=Constants.MOVEMENTS_INTERACT_KEY+video.getId().toHexString();
                String hashkey2=Constants.VIDEO_LIKE_HASHKEY+UserHolder.getUserId().toString();
                if(redisTemplate.opsForSet().isMember(key1,value1)){
                    vo.setHasFocus(1);
                }if(redisTemplate.opsForHash().hasKey(key2,hashkey2)){
                    vo.setHasLiked(1);
                }
                vos.add(vo);
            }
        }
        return new PageResult(page,pagesize,0,vos);
    }

    /**
     * 视频关注
     * @param uid 关注的用户id
     */
    public void guanzhu(Long uid) {
        //查询是否有这个用户
        UserInfo userInfo = userInfoApi.findById(uid);
        //如果查询不到,抛出异常
        if(userInfo==null){
            throw new BusinessException(ErrorResult.error());
        }
        //把关注的作者保存到redis中,用set集合好点
        String key =Constants.FOCUS_USER+UserHolder.getUserId().toString();
        String value=uid.toString();
        //保存到redis中
        redisTemplate.opsForSet().add(key,value);

        //查询到的话,就把当前用户id和关注的用户id放到MongoDB中
        //构建Focususer封装类
        Focususer focususer=new Focususer();
        //当前用户的id
        focususer.setUserId(UserHolder.getUserId());
        //关注的用户id
        focususer.setFollowUserId(uid);
        //关注时间
        focususer.setCreated(System.currentTimeMillis());
        //进行保存,并把保存之后的id返回,判断是否保存成功
        String id= focususerApi.guanzhu(focususer);
        if(StringUtils.isEmpty(id)){
            throw new BusinessException(ErrorResult.error());
        }

    }

    /**
     * 取消关注
     * @param uid 取消关注的用户id
     */
    public void quxiaoguanzhu(Long uid) {
        //构建key和value
        String key =Constants.FOCUS_USER+UserHolder.getUserId().toString();
        String value=uid.toString();
        //把redis中的删除
        redisTemplate.opsForSet().remove(key,value);
        //把数据库表中的删除
        focususerApi.quxiaoguanzhu(UserHolder.getUserId(),uid);
    }

    @Autowired
    private CommentApi commentApi;

    /**
     * 视频点赞
     * @param id 视频的id
     */
    public void like(String id) {
        System.out.println(id);
        //根据当前id判断对当前视频是否已经点过赞了
        boolean islike = commentApi.islike(id, UserHolder.getUserId(), CommentType.LIKE);
        //如果点赞过,返回的是true,抛出异常
        if(islike){
            throw  new BusinessException(ErrorResult.likeError());
        }
        //如果没有点赞,点赞标记亮起,mongodb评论表点赞数量加1
        //保存到Comment对象中
        Comment comment=new Comment();
        //保存动态id
        comment.setPublishId(new ObjectId(id));
        //保存动态的类型
        comment.setCommentType(CommentType.LIKE.getType());
        //保存点赞人
        comment.setUserId(UserHolder.getUserId());
        //发表时间
        comment.setCreated(System.currentTimeMillis());
        //进行保存,返回点赞数
       commentApi.savevideo(comment);
        //同时加入到redis中
        //拼接key
        String key=Constants.MOVEMENTS_INTERACT_KEY+id;
        //拼接hashkey
        String hashkey=Constants.VIDEO_LIKE_HASHKEY+UserHolder.getUserId();
        redisTemplate.opsForHash().put(key,hashkey,"2");
    }

    /**
     * 视频取消点赞
     * @param id 视频的id
     */
    public void nolike(String id) {
        //1、调用API查询用户是否已点赞
        Boolean hasComment = commentApi.islike(id, UserHolder.getUserId(), CommentType.LIKE);
        //2、如果未点赞，抛出异常
        if (!hasComment) {
            throw new BusinessException(ErrorResult.disLikeError());
        }
        //3、调用API，删除数据，返回点赞数量
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(id));
        comment.setCommentType(CommentType.LIKE.getType());
        comment.setUserId(UserHolder.getUserId());
         commentApi.deletevideo(comment);
        //4、拼接redis的key，删除点赞状态
        String key=Constants.MOVEMENTS_INTERACT_KEY+id;
        String hashkey=Constants.VIDEO_LIKE_HASHKEY+UserHolder.getUserId();
        System.out.println(hashkey);
        redisTemplate.opsForHash().delete(key, hashkey);
    }


    /**
     * 视频评论
     * @param id 视频id
     * @param context 评论内容
     */
    public void comments(String id, String context) {
        //根据当前评论id查询发布者的id
     Video video=   videoApi.findMovementByid(id);
        //把发布者id,视频id,当前用户id,评论内容保存到动态表
        //构建动态表对象
       Comment comment=new Comment();
       //视频的id
        comment.setPublishId(new ObjectId(id));
        //评论的类型
        comment.setCommentType(CommentType.COMMENT.getType());
        //评论的内容
        comment.setContent(context);
        //评论人
        comment.setUserId(UserHolder.getUserId());
        //被评论人ID
        comment.setPublishUserId(video.getUserId());
        //发布时间
        comment.setCreated(System.currentTimeMillis());
        commentApi.saveshipingcomments(comment);

    }
}
