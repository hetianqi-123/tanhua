package api;

import mongo.UserLike;

import java.util.List;

public interface UserLikeApi {
    /**
     * 查询有没有这个
     * @param userId
     * @param likeUserId
     * @return
     */
    boolean savelike(Long userId, Long likeUserId, boolean b);

    /**
     * 查询互相喜欢的好友列表
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    List<UserLike> geteachLoveCount(int page, int pagesize, Long userId);

    /**
     * 查询我喜欢的,但是不喜欢我的用户
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    List<UserLike> getloveCount(int page, int pagesize, Long userId);

    /**
     * 喜欢我的用户id
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
   List<UserLike> getfanCount(int page, int pagesize,  Long userId);

    /**
     * 取消喜欢
     * @param userId
     * @param uid
     */
    void CancelLike(Long userId, Long uid);

    /**
     * 喜欢粉丝
     * @param userId
     * @param uid
     */
    void addlove(Long userId, Long uid);

    /**
     * 用户互相喜欢的数量
     * @param userId
     * @return
     */
    long CountEachLoveCount(Long userId);

    /**
     * 我喜欢的数量
     * @param userId
     * @return
     */
    long CountLoveCount(Long userId);

    /**
     * 统计喜欢我的用户
     * @param userId
     * @return
     */
    long CountFanCount(Long userId);

    /**
     * 判断我是否喜欢这个好友
     * @param userId
     * @param userId1
     * @return
     */
    boolean islike(Long userId, Long userId1);
}
