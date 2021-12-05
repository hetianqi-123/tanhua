package api;

import mongo.Movement;
import vo.PageResult;

import java.util.List;

public interface MovementApi {
    /**
     * 发布动态
     * @param movement
     */
    String movements(Movement movement);


    /**
     * 查询个人动态
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult findById(int page, int pagesize, Long userId);

    /**
     * 查询好友动态
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    List<Movement> findFriendMovements(Long userId, int page, int pagesize);


    /**
     * 动态推荐好友---推荐的有pid
     * @return
     */
    List<Movement> findByPids(List<Long> pids);

    /**
     * 动态推荐好友---推荐的没有pid
     * @param pagesize
     * @return
     */
    List<Movement> randomMovements(int pagesize);

    /**
     * 查询单条动态
     * @param movementId
     * @return
     */
    Movement findMovementById(String movementId);


    /**
     * 查询用户的动态
     * @param uid
     * @param state
     * @param page
     * @param pagesize
     * @return
     */
    PageResult findByUserId(Long uid, Integer state, Integer page, Integer pagesize);

    //修改审核状态
    void update(String movementId, int state);
}
