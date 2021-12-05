package api;

import domain.User;

public interface UserApi {

    //根据手机号码查询用户
    User findByMobile(String mobile);

    //保存用户，返回用户id
    Long save(User user);

    //修改新手机号
    void update(User user);

    //查询用户的注册信息
    User findByid(Long userId);

    /**
     * 根据环信id查询用户注册信息
     * @param huanxinId
     * @return
     */
    User findhuanxinId(String huanxinId);
}
