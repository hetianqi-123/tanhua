package api;

import mongo.Focususer;

public interface FocususerApi {
    /**
     * 关注视频作者
     * @param focususer
     * @return
     */
    String guanzhu(Focususer focususer);

    /**
     * 取消关注
     * @param userId 当前用户id
     * @param uid 取消关注的用户id
     */
    void quxiaoguanzhu(Long userId, Long uid);
}
