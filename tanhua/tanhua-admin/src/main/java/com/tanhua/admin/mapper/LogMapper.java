package com.tanhua.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import domain.Log;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface LogMapper extends BaseMapper<Log> {
    /**
     *  根据类型统计用户登陆和注册的数量
     * @param type
     * @param logTime
     * @return
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM tb_log WHERE TYPE=#{type} AND log_time=#{logTime}")
    Integer queryByTypeAndLogTime(@Param("type") String type, @Param("logTime") String logTime); //根据操作时间和类型

    /**
     * 统计用户指定时间的活跃量
     * @param logTime
     * @return
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM tb_log WHERE log_time=#{logTime}")
    Integer queryByLogTime(String logTime); //展示记录时间查询


    /**
     *  统计次日留存： 表示昨天注册了，今天活跃
     * @param today
     * @param yestoday
     * @return
     */
    @Select("SELECT COUNT(DISTINCT user_id)  FROM tb_log WHERE log_time=#{today} AND user_id IN (\n " +
            " SELECT user_id FROM tb_log WHERE TYPE='0102' AND log_time=#{yestoday} \n " +
            ")")
    Integer queryNumRetention1d(@Param("today") String today,@Param("yestoday") String yestoday); //查询次日留存

}
