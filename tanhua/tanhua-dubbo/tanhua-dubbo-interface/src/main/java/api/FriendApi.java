package api;

import mongo.Friend;

import java.util.List;

public interface FriendApi {
    /**
     * 添加好友
     * @param userId 当前用户id
     * @param friendId 好友id
     */
    void save(Long userId, Long friendId);

    /**
     * 好友列表显示
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    List<Friend> findcontacts(Integer page, Integer pagesize, Long userId);


    /**
     * 删除好友
     * @param friendId  要删除的好友
     * @param userId 当前用户id
     */
    void delete(Long friendId, Long userId);
}

