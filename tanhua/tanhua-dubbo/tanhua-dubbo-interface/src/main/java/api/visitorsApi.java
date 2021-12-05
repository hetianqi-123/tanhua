package api;

import mongo.Visitors;

import java.util.List;

public interface visitorsApi {
    //保存访客信息
    void save(Visitors visitors);

    /**
     * 查询首页的访客
     * @param date 最后一次查询的时间
     * @param userId 当前用户的id
     * @return
     */
    List<Visitors> queryMyVisitors(Long date, Long userId);
}
