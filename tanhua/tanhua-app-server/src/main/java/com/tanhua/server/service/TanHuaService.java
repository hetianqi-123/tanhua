package com.tanhua.server.service;

import api.*;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.aliyuncs.utils.StringUtils;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import domain.Count;
import domain.Question;
import domain.UserInfo;
import dto.RecommendUserDto;
import mongo.RecommendUser;
import mongo.UserLike;
import mongo.Visitors;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import utils.Constants;
import vo.*;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TanHuaService {

    //调用mongodb数据库的今日佳人信息表
    @DubboReference
    private RecommendUserApi recommendUserApi;

    //陌生人问题表
    @DubboReference
    private QuestionApi questionApi;

    //调用mysql的个人详细信息表
    @DubboReference
    private UserInfoApi userInfoApi;
    //环信的工具类
    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @Autowired
    private UserlocationApi userlocationApi;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @DubboReference
    private UserLikeApi userLikeApi;

    @DubboReference
    private visitorsApi visitorsApi;



    /**
     * 查询今日佳人
     */
    public TodayBest FindWind() {
        //获取到当前登录的用户id
        Long userId = UserHolder.getUserId();
        //获取mongodb的今日佳人
        RecommendUser recommendUser = recommendUserApi.queryWithMaxScore(userId);
        //判断当前用户是否有今日佳人
        if(recommendUser==null){
            //如果没有的话,就生成一个默认的
            recommendUser =new RecommendUser();
            recommendUser.setUserId(1l);
            recommendUser.setScore(99d);
        }
        //根据分数最高的佳人id去查询对应的佳人的详细信息
        UserInfo userInfo = userInfoApi.findById(recommendUser.getUserId());
        //创建todayBest,vo类,返回前端参数的类(init写好的一个可以直接替换的方法)
        TodayBest todayBest = TodayBest.init(userInfo, recommendUser);
        //返回
        return  todayBest;

    }



    /**
     * 查询朋友分页列表 ----优化查询
     * @param dto
     * @return
     */
    //查询分页推荐好友列表
    public PageResult recommendation(RecommendUserDto dto) {
        //1、获取用户id
        Long userId = UserHolder.getUserId();
        //2、调用recommendUserApi分页查询数据列表
        PageResult pr = recommendUserApi.recommendationList(dto.getPage(),dto.getPagesize(),userId);
        //3、获取分页中的RecommendUser数据列表
        List<RecommendUser> items = (List<RecommendUser>) pr.getItems();
        //4、判断列表是否为空
        if(items == null) {
            return pr;
        }
        //5、提取所有推荐的用户id列表
        List<Long> ids = CollUtil.getFieldValues(items, "userId", Long.class);
        UserInfo userInfo = new UserInfo();
        userInfo.setAge(dto.getAge());
        userInfo.setGender(dto.getGender());
        //6、构建查询条件，批量查询所有的用户详情
        Map<Long, UserInfo> map = userInfoApi.finByIds(ids, userInfo);
        //7、循环推荐的数据列表，构建vo对象
        List<TodayBest> list = new ArrayList<>();
        for (RecommendUser item : items) {
            UserInfo info = map.get(item.getUserId());
            if(info!=null) {
                TodayBest vo = TodayBest.init(info, item);
                list.add(vo);
            }
        }
        //8、构造返回值
        pr.setItems(list);
        return pr;
    }

    /**
     * 查看佳人信息
     * @param userId 佳人id
     * @return
     */
    public TodayBest findjiaren(Long userId) {
        //1、根据用户id查询，用户详情
        UserInfo userInfo = userInfoApi.findById(userId);
        //2、根据操作人toUserId和查看的用户userId这两个条件查询唯一佳人 查询的recommenduser表
        RecommendUser recommendUser = recommendUserApi.queryByUserId(userId,UserHolder.getUserId());
        //构造访客数据
        Visitors visitors=new Visitors();
        //我的id,就是今日佳人的id
        visitors.setUserId(userId);
        //来访用户id,就是当前操作用户的id
        visitors.setVisitorUserId(UserHolder.getUserId());
        //首页
        visitors.setFrom("首页");
        //来访时间
        visitors.setDate(System.currentTimeMillis());
        //来访日期
        visitors.setVisitDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        //缘分值
        visitors.setScore(recommendUser.getScore());
        //进行保存
        visitorsApi.save(visitors);

        //3、构造返回值
        return   TodayBest.init(userInfo,recommendUser);
    }


    /**
     * 查看陌生人的问题
     * @param userId 陌生人的id
     * @return
     */
    public String findmswenti(Long userId) {
        //查询陌生人问题表
        Question question = questionApi.findByUserId(userId);
        return question == null ? "你喜欢java编程吗？" : question.getTxt();
    }


    /**
     * 回复陌生人信息
     * @param userId 陌生人的id
     * @param reply 回复的问题
     */
    public void replyQuestions(Long userId, String reply) {
        //因为发送到环信是这种的json格式,所以需要搭建这个格式的数据
//        {"userId":106,"huanXinId":"hx106","nickname":"黑马小妹",
//         "strangerQuestion":"你喜欢去看蔚蓝的大海还是去爬巍峨的高山？",
//        "reply":"我喜欢秋天的落叶，夏天的泉水，冬天的雪地，只要有你一切皆可~"}
        //获取到当前id
        Long currentUserId = UserHolder.getUserId();
        //根据当前用户id获取到详细信息
        UserInfo userInfo = userInfoApi.findById(currentUserId);
        //把消息放到map中,方便转换json
        Map map = new HashMap();
        map.put("userId",currentUserId);
        map.put("huanXinId", Constants.HX_USER_PREFIX+currentUserId);
        map.put("nickname",userInfo.getNickname());
        //调用查看陌生人的问题
        map.put("strangerQuestion",findmswenti(userId));
        map.put("reply",reply);
        //把map转换成json格式的String类型
        String message = JSON.toJSONString(map);
        //进行发送消息  环信的用户名和发送的消息
        Boolean aBoolean = huanXinTemplate.sendMsg(Constants.HX_USER_PREFIX + userId, message);
        //判断是否发送成功
        if(!aBoolean){
            throw  new BusinessException(ErrorResult.error());
        }

    }


    /**
     * 左滑右滑功能
     * @return
     */

    //默认的推荐用户
    @Value("${tanhua.default.recommend.users}")
    private String recommendUser;
    public List<TodayBest> zuoyouhua() {
        //查询推荐的用户列表,进行数量限制
        List<RecommendUser> users= recommendUserApi.islike(UserHolder.getUserId(),10);
        //判断list是否为空
        if(CollUtil.isEmpty(users)){
            //如果是null构建默认数据
            users=new ArrayList<>();
            //上面的recommendUser是默认推荐的用户的id
            String[] split = recommendUser.split(",");
            for (String s : split) {
                RecommendUser recommendUser = new RecommendUser();
                //推荐的用户id
                recommendUser.setUserId(Convert.toLong(s));
                //本地用户id
                recommendUser.setToUserId(UserHolder.getUserId());
                //缘分值随机生成
                recommendUser.setScore(RandomUtil.randomDouble(60, 90));
                users.add(recommendUser);
            }
        }
        List<Long> ids = CollUtil.getFieldValues(users, "userId", Long.class);
        Map<Long, UserInfo> infoMap = userInfoApi.finByIds(ids, null);

        List<TodayBest> vos = new ArrayList<>();
        for (RecommendUser user : users) {
            UserInfo userInfo = infoMap.get(user.getUserId());
            if(userInfo != null) {
                TodayBest vo = TodayBest.init(userInfo, user);
                vos.add(vo);
            }
        }
        return vos;
    }
    @Autowired
    private MessagesService messagesService;
    /**
     * 右滑喜欢
     * @param likeUserId 喜欢的用户编号
     * @return
     */
    public void love(Long likeUserId) {
        //去查询有没有当前这个用户
      boolean save = userLikeApi.savelike(UserHolder.getUserId(),likeUserId,true);
      //如果没有
        if(!save){
            //失败
            throw new BusinessException(ErrorResult.error());
        }
        //如果有的话,把数据保存到redis中
        //先从不喜欢里面删除,再加到喜欢里面
        redisTemplate.opsForSet().remove(Constants.USER_NOT_LIKE_KEY+UserHolder.getUserId(),likeUserId.toString());
        redisTemplate.opsForSet().add(Constants.USER_LIKE_KEY+UserHolder.getUserId(),likeUserId.toString());
        //判断下是否是双向喜欢
        if(isLike(likeUserId,UserHolder.getUserId())) {
            //添加好友
            messagesService.contacts(likeUserId);
        }
    }
    /**
     * 判断是否双向喜欢
     * @param userId 喜欢我的好友id
     * @param likeUserId 我的id
     * @return
     */
    public Boolean isLike(Long userId,Long likeUserId) {
        String key = Constants.USER_LIKE_KEY+userId;
        //判断是否存在set集合中
        return redisTemplate.opsForSet().isMember(key,likeUserId.toString());
    }

    /**
     * 不喜欢
     * @param likeUserId 不喜欢的好友id
     *
     */
    public void notLikeUser(Long likeUserId) {
        //调用API，保存喜欢数据(保存到MongoDB中)
        Boolean save = userLikeApi.savelike(UserHolder.getUserId(),likeUserId,false);
        if(!save) {
            //失败
            throw new BusinessException(ErrorResult.error());
        }
        //2、操作redis，写入喜欢的数据，删除不喜欢的数据 (喜欢的集合，不喜欢的集合)
        redisTemplate.opsForSet().add(Constants.USER_NOT_LIKE_KEY+UserHolder.getUserId(),likeUserId.toString());
        redisTemplate.opsForSet().remove(Constants.USER_LIKE_KEY+UserHolder.getUserId(),likeUserId.toString());
        //判断是否双向不喜欢，删除好友
        //先判断是否为双向好友
        if(notLike(likeUserId,UserHolder.getUserId())){
            //如果不喜欢列表都存在,进行好友列表删除
            messagesService.delete(likeUserId);
            }

        }

    /**
     * 判断双发的拉黑列表是否有对方
     * @param likeUserId 不喜欢好友的id
     * @param userId 自己的id
     * @return
     */
    public boolean notLike(Long likeUserId,Long userId ){
        String key = Constants.USER_NOT_LIKE_KEY+userId;
        //判断是否存在set集合中
      return redisTemplate.opsForSet().isMember(key,likeUserId.toString());
    }


    /**
     * 搜索附近
     * @param gender 性别
     * @param distance 距离
     * @return
     */
    public List<NearUserVo> search(String gender, String distance) {
        //根据当前用户id和距离查询附近的人,获取到附近人的id
       List<Long> userids= userlocationApi.search(UserHolder.getUserId(),Double.valueOf(distance));
       //判断是否有数据
       if(CollUtil.isEmpty(userids)){
           //返回空
           return new ArrayList<>();
       }
       //如果有数据根据id和性别查询用户的详细信息
        UserInfo userInfo=new UserInfo();
        userInfo.setGender(gender);
        //获取到指定条件查询的用户详细信息
        Map<Long, UserInfo> longUserInfoMap = userInfoApi.finByIds(userids, userInfo);
        //保存到NearUserVo封装类中
        List<NearUserVo> vos =new ArrayList<>();
        //遍历附近距离的用户id
        for (Long userid : userids) {
            //这个时候会获取到自己的id,进行排除
            if(userid==UserHolder.getUserId()){
                //跳过这次循环
                continue;
            }
            UserInfo userInfo1 = longUserInfoMap.get(userid);
            //如果取出的用户信息不为空
            if(userInfo1 != null){
                //保存到NearUserVo中
                NearUserVo vo = NearUserVo.init(userInfo1);
                //再保存到集合中
                vos.add(vo);
            }

        }
        //返回集合
        return vos;

    }


    /**
     * 喜欢统计用户列表
     * @param type
     * @param page
     * @param pagesize
     * @param nickname
     * @return
     */
    //1 互相关注
    //2 我关注
    //3 粉丝
    //4 谁看过我
    public PageResult countslikelist(int type, int page, int pagesize, String nickname) {
        List<friendsVo> list = new ArrayList<>();
        //判断查询哪个页面
        switch (type) {
            //查询互相关注的
            case 1: {
                //获取到互相喜欢的好友列表
                List<UserLike> userLikes = userLikeApi.geteachLoveCount(page, pagesize, UserHolder.getUserId());
                if(CollUtil.isEmpty(userLikes)){
                    return new PageResult();
                }
                UserInfo userInfo = new UserInfo();
                userInfo.setNickname(nickname);
                //获取到互相喜欢的好友的id
                List<Long> userIds = CollUtil.getFieldValues(userLikes, "userId", Long.class);
                //查询详细信息
                Map<Long, UserInfo> map = userInfoApi.finByIds(userIds, userInfo);
                //查询互相喜欢所有好友的匹配度
                Map<Long, RecommendUser> scoresMap = recommendUserApi.finByIds(userIds, UserHolder.getUserId());
                //遍历互相喜欢的好友id
                for (Long userId : userIds) {
                    friendsVo friendsVo = new friendsVo();
                    UserInfo userInfo1 = map.get(userId);
                    if (userInfo1 != null) {
                        friendsVo.setAge(userInfo1.getAge());
                        friendsVo.setAvatar(userInfo1.getAvatar());
                        friendsVo.setEducation(userInfo1.getEducation());
                        friendsVo.setGender(userInfo1.getGender());
                        friendsVo.setId(userInfo1.getId());
                        friendsVo.setMarriage(userInfo1.getMarriage());
                        friendsVo.setNickname(userInfo1.getNickname());
                        RecommendUser recommendUser = scoresMap.get(userId);
                        if (recommendUser != null) {
                            Double score = recommendUser.getScore();
                            //互相喜欢好友的好感度
                            //Integer.valueOf(String.valueOf(score))
                            friendsVo.setMatchRate(Convert.toInt(score));
                        }else {
                            friendsVo.setMatchRate(0);
                        }
                        friendsVo.setAlreadyLove(userLikeApi.islike(UserHolder.getUserId(),userId));
                    }else {
                        continue;
                    }

                    list.add(friendsVo);
            }

                break;
            }
            //查询我喜欢的,但是不喜欢我的用户
            case 2:{
                List<UserLike> userLikes = userLikeApi.getloveCount(page, pagesize, UserHolder.getUserId());
                if(CollUtil.isEmpty(userLikes)){
                    return new PageResult();
                }
                UserInfo userInfo = new UserInfo();
                userInfo.setNickname(nickname);
                //获取到我喜欢的好友id
                List<Long> userIds = CollUtil.getFieldValues(userLikes, "likeUserId", Long.class);
                //查询详细信息
                Map<Long, UserInfo> map = userInfoApi.finByIds(userIds, userInfo);
                //查询我喜欢的好友的匹配度
                Map<Long, RecommendUser> scoresMap = recommendUserApi.finByIds(userIds, UserHolder.getUserId());
                //遍历我喜欢的人id
                for (Long userId : userIds) {
                    friendsVo friendsVo = new friendsVo();
                    //获取到我喜欢的好友的详细信息
                    UserInfo userInfo1 = map.get(userId);
                    if (userInfo1 != null) {
                        friendsVo.setAge(userInfo1.getAge());
                        friendsVo.setAvatar(userInfo1.getAvatar());
                        friendsVo.setEducation(userInfo1.getEducation());
                        friendsVo.setGender(userInfo1.getGender());
                        friendsVo.setId(userInfo1.getId());
                        friendsVo.setMarriage(userInfo1.getMarriage());
                        friendsVo.setNickname(userInfo1.getNickname());
                        RecommendUser recommendUser = scoresMap.get(userId);
                        if (recommendUser != null) {
                            Double score = recommendUser.getScore();
                            //互相喜欢好友的好感度
                            //Integer.valueOf(String.valueOf(score))
                            friendsVo.setMatchRate(Convert.toInt(score));
                        }else {
                            friendsVo.setMatchRate(0);
                        }
                        //判读是否喜欢他
                        friendsVo.setAlreadyLove(userLikeApi.islike(UserHolder.getUserId(),userId));
                    }else {
                        continue;
                    }
                    list.add(friendsVo);
                }
                break;
            }
            //查询喜欢我的,但是我不喜欢他的
            case 3:{
                List<UserLike> userLikes = userLikeApi.getfanCount(page, pagesize, UserHolder.getUserId());
                if(CollUtil.isEmpty(userLikes)){
                    return new PageResult();
                }
                UserInfo userInfo = new UserInfo();
                userInfo.setNickname(nickname);
                //获取到互相喜欢的好友的id
                List<Long> userIds = CollUtil.getFieldValues(userLikes, "userId", Long.class);
                //查询详细信息
                Map<Long, UserInfo> map = userInfoApi.finByIds(userIds, userInfo);
                //查询互相喜欢所有好友的匹配度
                Map<Long, RecommendUser> scoresMap = recommendUserApi.finByIds(userIds, UserHolder.getUserId());
                //遍历互相喜欢的好友id
                for (Long userId : userIds) {
                    friendsVo friendsVo = new friendsVo();
                    UserInfo userInfo1 = map.get(userId);
                    if (userInfo1 != null) {
                        friendsVo.setAge(userInfo1.getAge());
                        friendsVo.setAvatar(userInfo1.getAvatar());
                        friendsVo.setEducation(userInfo1.getEducation());
                        friendsVo.setGender(userInfo1.getGender());
                        friendsVo.setId(userInfo1.getId());
                        friendsVo.setMarriage(userInfo1.getMarriage());
                        friendsVo.setNickname(userInfo1.getNickname());
                        RecommendUser recommendUser = scoresMap.get(userId);
                        if (recommendUser != null) {
                            Double score = recommendUser.getScore();
                            //互相喜欢好友的好感度
                            //Integer.valueOf(String.valueOf(score))
                            friendsVo.setMatchRate(Convert.toInt(score));
                        }else {
                            friendsVo.setMatchRate(0);
                        }
                        friendsVo.setAlreadyLove(userLikeApi.islike(UserHolder.getUserId(),userId));
                    }
                    list.add(friendsVo);
                }
                break;
            }
            case 4:{
                //从redis中获取到上次的访问时间
                //获取key和hashkey
                String key =Constants.VISITORS_USER;
                String hashkey = String.valueOf(UserHolder.getUserId());
                //根据key和hashkey获取当前用户上次访问的时间
                String  value = (String) redisTemplate.opsForHash().get(key, hashkey);
                //判断时间是否为null,不为null转换为long类型,用来比较大小
                Long date = StringUtils.isEmpty(value) ? null : Long.valueOf(value);
                //根据上次的时间和当前用户id查询多个访客
                List<Visitors> list2=  visitorsApi.queryMyVisitors(date,UserHolder.getUserId());
                //判断访客是否为空
                if(CollUtil.isEmpty(list2)){
                    return new PageResult();
                }
                //不为空的话,提取访客id查询访客的详细信息
                List<Long> userIds = CollUtil.getFieldValues(list2, "visitorUserId", Long.class);
                //去一次性查询详细信息
                Map<Long, UserInfo> longUserInfoMap = userInfoApi.finByIds(userIds, null);
                //查询访客的匹配度
                Map<Long, RecommendUser> scoresMap = recommendUserApi.finByIds(userIds, UserHolder.getUserId());
                for (Long userId : userIds) {
                    friendsVo friendsVo = new friendsVo();
                    UserInfo userInfo1 = longUserInfoMap.get(userId);
                    if (userInfo1 != null) {
                        friendsVo.setAge(userInfo1.getAge());
                        friendsVo.setAvatar(userInfo1.getAvatar());
                        friendsVo.setEducation(userInfo1.getEducation());
                        friendsVo.setGender(userInfo1.getGender());
                        friendsVo.setId(userInfo1.getId());
                        friendsVo.setMarriage(userInfo1.getMarriage());
                        friendsVo.setNickname(userInfo1.getNickname());
                        RecommendUser recommendUser = scoresMap.get(userId);
                        if (recommendUser != null) {
                            Double score = recommendUser.getScore();
                            //互相喜欢好友的好感度
                            //Integer.valueOf(String.valueOf(score))
                            friendsVo.setMatchRate(Convert.toInt(score));
                        }else {
                            friendsVo.setMatchRate(0);
                        }
                        friendsVo.setAlreadyLove(userLikeApi.islike(UserHolder.getUserId(),userId));
                    }
                    list.add(friendsVo);
                }
                break;
            }
            default:
                return new PageResult();

        }
        return new PageResult(page, pagesize, 0, list);
    }

    /**
     * 取消喜欢
     * @param uid
     */
    public void CancelLike(Long uid) {
        //从redis进行删除
        //拼接key
        String key=Constants.USER_LIKE_KEY+UserHolder.getUserId();
        //从redis删除
        redisTemplate.opsForSet().remove(key,uid.toString());
        //从MongoDB中删除
        userLikeApi.CancelLike(UserHolder.getUserId(),uid);
        //删除好友关系
        //1.环信要删除好友关系,在MongoDB中进行删除
        messagesService.delete(uid);
    }

    /**
     * 喜欢粉丝
     * @param uid
     */
    public void like(Long uid) {
        //添加到redis
        //拼接key
        String key=Constants.USER_LIKE_KEY+UserHolder.getUserId();
        redisTemplate.opsForSet().add(key,uid.toString());
        //添加到MongoDB中
        userLikeApi.addlove(UserHolder.getUserId(),uid);
        //在环信中加好友
        //MongoDB中加好友
        messagesService.contacts(uid);
    }



    /**互相喜欢，喜欢，粉丝 - 统计
     *
     * @return
     */
    public Count countslike() {
    //获取到当前用户id
        Long userId = UserHolder.getUserId();
        //查询互相喜欢数
        long eachLoveCount= userLikeApi.CountEachLoveCount(userId);
        //查询我喜欢的数量
        long loveCount= userLikeApi.CountLoveCount(userId);
        //查询喜欢我的
        long fanCount= userLikeApi.CountFanCount(userId);
        //封装到封装类,返回前端
        Count count=new Count();
        count.setEachLoveCount(Convert.toInt(eachLoveCount));
        count.setLoveCount(Convert.toInt(loveCount));
        count.setFanCount(Convert.toInt(fanCount));
        return count;
    }



}
