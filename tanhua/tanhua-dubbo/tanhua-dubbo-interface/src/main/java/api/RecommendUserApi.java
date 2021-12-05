package api;

import mongo.RecommendUser;
import vo.PageResult;

import java.util.List;
import java.util.Map;

public interface RecommendUserApi {

    /**
     * 查询今日佳人
     * @param toUserId
     * @return
     */
    RecommendUser queryWithMaxScore(Long toUserId);

    /**
     * 根据id进行分页查询好友
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult recommendationList(Integer page, Integer pagesize, Long userId);


    /**
     * 根据当前用户id和佳人id查询佳人信息
     * @param userId 佳人id
     * @param touserId 当前用户id
     * @return
     */
    RecommendUser queryByUserId(Long userId, Long touserId);

    /**
     * 左滑右滑,排除不喜欢,或者新欢的好友
     * @param userId
     * @param i
     * @return
     */
    List<RecommendUser> islike(Long userId, int i);


    /**
     * 查询互相喜欢好友的好感度
     * @param userIds
     * @param userId
     * @return
     */
    Map<Long, RecommendUser> finByIds(List<Long> userIds, Long userId);
}