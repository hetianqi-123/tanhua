package com.tanhua.server.service;

import api.CommentApi;
import api.MovementApi;
import api.UserInfoApi;
import api.visitorsApi;
import cn.hutool.core.collection.CollUtil;
import com.aliyuncs.utils.StringUtils;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import domain.UserInfo;
import enums.CommentType;
import mongo.Comment;
import mongo.Movement;
import mongo.Visitors;
import org.apache.dubbo.config.annotation.DubboReference;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import utils.Constants;
import vo.ErrorResult;
import vo.MovementsVo;
import vo.PageResult;
import vo.VisitorsVo;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovementsService {
    @DubboReference
    private MovementApi movementApi;
    @DubboReference
    private UserInfoApi userInfoApi;
    @DubboReference
    private visitorsApi visitorsApi;

    @Autowired
    private OssTemplate ossTemplate;
    @Autowired
    private CommentApi commentApi;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 发布个人动态
     * @param movement
     * @param imageContent
     * @throws IOException
     */
    public void movements(Movement movement, MultipartFile[] imageContent) throws IOException {
        //判断发送的内容是否包含文字
        if(StringUtils.isEmpty(movement.getTextContent())){
            throw  new RuntimeException("发送的内容不能为空");
        }
        //获取到当前用户id
        Long userId = UserHolder.getUserId();
        //创建集合,用来保存用户上传的多张照片的url地址
        List<String> listurl=new ArrayList();
        //遍历用户上传的照片数组,把文件一一上传到阿里云
        for (MultipartFile multipartFile : imageContent) {
            String url = ossTemplate.upload(multipartFile.getOriginalFilename(), multipartFile.getInputStream());
           //保存用户上传阿里云之后的多张照片地址
            listurl.add(url);
        }
        //把数据封装到Movement封装类中
        movement.setUserId(userId);
        movement.setMedias(listurl);
        //movement剩下的属性在调用业务实现类赋值"
        String movementId = movementApi.movements(movement);
        //通过消息队列发送,去审核内容
        mqMessageService.sendAudiMessage(movementId);
        //发送队列消息
        mqMessageService.sendLogMessage(UserHolder.getUserId(),"0201","movement",movementId);
    }

    /**
     * 查询个人动态
     * @param page 当前页
     * @param pagesize 每页显示的数据条数
     * @param userId 当前登录的用户id
     * @return
     */
    public PageResult all(int page, int pagesize, Long userId) {
        //查询id
        System.out.println("userId"+userId);
        //1、根据用户id，调用API查询个人动态内容（PageResult  -- Movement）
        PageResult re= movementApi.findById(page,pagesize,userId);
        //获取到当前用户分页之后的所有动态
        List<Movement> items = (List<Movement>) re.getItems();
        for (Movement item : items) {
            System.out.println("item--------------------->"+item);
        }
        //判断是否为空
        if(items==null){
            return re;
        }
        //如果不为null
        //获取当前用户的详细信息
        UserInfo userInfo = userInfoApi.findById(userId);
        //创建MovementsVo集合对象,用来保存多个返回对象
        List<MovementsVo> vos = new ArrayList<>();
        for (Movement item : items) {
            MovementsVo vo = MovementsVo.init(userInfo, item);
            vos.add(vo);
        }

        //都保存到PageResult中的数据源中
        re.setItems(vos);
        //构建返回对象
        return  re;
    }

    /**
     * 查看好友动态
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult friendsmovements(int page, int pagesize) {
        //获取到当前用户id
        Long userId = UserHolder.getUserId();
        //根据当前用户id查询出所有的详情
        List<Movement> items = movementApi.findFriendMovements(userId,page,pagesize);
        return getPageResult(page, pagesize, items);
    }

    /**
     * 公共的方法
     * @param page
     * @param pagesize
     * @param items
     * @return
     */
    private PageResult getPageResult(int page, int pagesize, List<Movement> items) {
        //判断当前用户是否有好友动态
        if(CollUtil.isEmpty(items)){
        return new PageResult();
        }
        //如果存在,获取好友的详细信息
        List<Long> userIds = CollUtil.getFieldValues(items, "userId", Long.class);
        Map<Long, UserInfo> longUserInfoMap = userInfoApi.finByIds(userIds, null);
        List<MovementsVo> vos = new ArrayList<>();
        //遍历好友动态详情
        for (Movement movement : items) {
            //取出当前到动态的好友的信息
            UserInfo userInfo = longUserInfoMap.get(movement.getUserId());
            if(userInfo != null) {
                MovementsVo vo = MovementsVo.init(userInfo, movement);
                //修复点赞的bug
                String key = Constants.MOVEMENTS_INTERACT_KEY + movement.getId().toHexString();
                System.out.println("点赞的bug的key--------------------->"+key);
                String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();
                System.out.println("点赞的hashKey--------------------->"+hashKey);
                //如果获取到key,修改点赞状态为1
                //修复喜欢的bug
                String key1 = Constants.MOVEMENTS_INTERACT_KEY + movement.getId().toHexString();
                String hashKey1 = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getUserId();
                if(redisTemplate.opsForHash().hasKey(key,hashKey)){
                    vo.setHasLiked(1);
                }
                if(redisTemplate.opsForHash().hasKey(key1,hashKey1)){
                    vo.setHasLoved(1);
                }
                System.out.println(vo.getHasLiked());
                vos.add(vo);
            }
        }
        return  new PageResult(page, pagesize,0,vos);
    }

    /**
     * 推荐动态
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult recommended(int page, int pagesize) {
        //获取当前用户id
        String redisKey = "MOVEMENTS_RECOMMEND_" + UserHolder.getUserId();
        //从redis中获取推荐的数据
        String userid = this.redisTemplate.opsForValue().get(redisKey);
        //创建个集合
        List<Movement> list = Collections.EMPTY_LIST;
        //判断redis中有没有推荐动态的好友id
        if(StringUtils.isEmpty(userid)){
            //没有的话随机生成十个
            list=movementApi.randomMovements(pagesize);
        }else {
            //把多个id拆分为数组
            String[] split = userid.split(",");
            //进行内存分组,并把每个id转化为long类型
            if((page-1)*pagesize < split.length){
                List<Long> pids = Arrays.stream(split).limit(pagesize).skip((page - 1) * pagesize)
                        .map(e -> Long.valueOf(e))
                        .collect(Collectors.toList());
                list = movementApi.findByPids(pids);
            }
            List<Long> userIds = CollUtil.getFieldValues(list, "userId", Long.class);
        }
        return   getPageResult(page, pagesize, list);
    }

    @Autowired
    private MqMessageService mqMessageService;
    /**
     * 查询单条动态
     * @param movementId
     * @return
     */
    public MovementsVo findMovementById(String movementId) {
        //消息队列发送消息
        mqMessageService.sendLogMessage(UserHolder.getUserId(),"0202","movement",movementId);

        Movement movements = movementApi.findMovementById(movementId);
        //判断是否查询到动态
        if(movements == null) {
            return null;
        }else {
            //获取到当前发布动态的人的id
            Long userId = movements.getUserId();
            //根据id查询详细个人信息
            UserInfo userInfo = userInfoApi.findById(userId);
            return MovementsVo.init(userInfo,movements);
        }


    }

    /**
     * 点赞
     * @param movementId 动态id
     * @return
     */
    public Integer like(String movementId) {

        //根据当前用户id,动态id,动态类型 去查询当前动态是否已经点过赞
          boolean islike= commentApi.islike(movementId,UserHolder.getUserId(), CommentType.LIKE);
        //如果点赞过,返回的是true,抛出异常
        if(islike){
            throw  new BusinessException(ErrorResult.likeError());
        }
        //如果没有点赞,点赞标记亮起,mongodb评论表点赞数量加1
        //保存到Comment对象中
        Comment comment=new Comment();
        //保存动态id
        comment.setPublishId(new ObjectId(movementId));
        //保存动态的类型
        comment.setCommentType(CommentType.LIKE.getType());
        //保存评论人
        comment.setUserId(UserHolder.getUserId());
        //发表时间
        comment.setCreated(System.currentTimeMillis());
        //进行保存,返回点赞数
        Integer count = commentApi.save(comment);
        //同时加入到redis中
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        System.out.println("点赞的key--------------------->"+key);
        String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();
        System.out.println("点赞的key--------------------->"+hashKey);
        redisTemplate.opsForHash().put(key,hashKey,"1");
        return count;
    }

    //取消点赞
    public Integer dislikeComment(String movementId) {
        //1、调用API查询用户是否已点赞
        Boolean hasComment = commentApi.islike(movementId, UserHolder.getUserId(), CommentType.LIKE);
        //2、如果未点赞，抛出异常
        if (!hasComment) {
            throw new BusinessException(ErrorResult.disLikeError());
        }
        //3、调用API，删除数据，返回点赞数量
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setCommentType(CommentType.LIKE.getType());
        comment.setUserId(UserHolder.getUserId());
        Integer count = commentApi.delete(comment);
        //4、拼接redis的key，删除点赞状态
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        System.out.println(key);
        String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();
        System.out.println(hashKey);
        redisTemplate.opsForHash().delete(key, hashKey);
        return count;
    }



    /**
     * 首页----谁看过我
     * @return
     */
    public List<VisitorsVo> queryVisitorsList() {
        //查询看过我的人的详细信息和缘分值
        //从reids中查询上次的访问时间
        //获取key和hashkey
        String key =Constants.VISITORS_USER;
        String hashkey = String.valueOf(UserHolder.getUserId());
        //根据key和hashkey获取当前用户上次访问的时间
        String  value = (String) redisTemplate.opsForHash().get(key, hashkey);
        //判断时间是否为null,不为null转换为long类型,用来比较大小
        Long date = StringUtils.isEmpty(value) ? null : Long.valueOf(value);
        //根据上次的时间和当前用户id查询多个访客
        List<Visitors> list=  visitorsApi.queryMyVisitors(date,UserHolder.getUserId());
        //判断访客是否为空
        if(CollUtil.isEmpty(list)){
            return new ArrayList<>();
        }
        //不为空的话,提取访客id查询访客的详细信息
        List<Long> userId = CollUtil.getFieldValues(list, "visitorUserId", Long.class);
        //去一次性查询详细信息
        Map<Long, UserInfo> longUserInfoMap = userInfoApi.finByIds(userId, null);
        //创建VisitorsVo集合
      List<VisitorsVo> vos=new ArrayList<>();
        for (Visitors visitors : list) {
            UserInfo userInfo = longUserInfoMap.get(visitors.getVisitorUserId());
            if(userInfo!=null){
                VisitorsVo vo = VisitorsVo.init(userInfo, visitors);
                vos.add(vo);
            }
        }

        return vos;
    }
}
