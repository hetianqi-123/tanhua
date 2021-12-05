package api;

import com.baomidou.mybatisplus.core.metadata.IPage;

public interface BlackListApi {
    IPage findByUserId(Long userId, int page, int pagesize);

    /**
     * 移除黑名单
     * @param blackUserId
     * @param userId
     */
    void deletefanye(Long blackUserId,Long userId);
}
