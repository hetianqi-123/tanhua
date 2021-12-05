package api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import domain.UserInfo;

import java.util.List;
import java.util.Map;

public interface UserInfoApi {
    public void save(UserInfo userInfo);

    public void update(UserInfo userInfo);

    UserInfo findById(Long id);


    /**
     * 批量查询用户详情
     */
    Map<Long,UserInfo> finByIds(List<Long> userid,UserInfo userInfo);

    /**
     * 查询后台用户列表
     * @param page
     * @param pagesize
     * @return
     */
    IPage<UserInfo> findAll(Integer page, Integer pagesize);
}
