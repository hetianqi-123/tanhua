package com.tanhua.admin.mapper;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import domain.Analysis;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AnalysisMapper extends BaseMapper<Analysis> {

    /**
     * 查询今年的注册数
     * @param startDate 开始日期
     * @param endDate  结束日期
     * @return
     */
    @Select("SELECT   sum(num_registered) as num_registered FROM tb_analysis  " +
            " WHERE record_date >= #{startDate} AND record_date <= #{endDate}\n")
    Integer selectnum_registered(@Param("startDate") DateTime startDate,@Param("endDate")DateTime endDate);


    /**
     * 查询去年的注册数
     * @param startDate
     * @param endDate
     * @return
     */
    @Select("select  sum(num_registered) as num_registered from  tb_analysis where record_date >= DATE_ADD(#{startDate}, INTERVAL -1 YEAR)" +
            "AND record_date <= DATE_ADD(#{endDate}, INTERVAL -1 YEAR)")
    Integer selectnum_registeredtoyear(@Param("startDate") DateTime startDate,@Param("endDate")DateTime endDate);

    /**
     * 查询本年活跃数
     * @param startDate
     * @param endDate
     * @return
     */
    @Select("SELECT   sum(num_active) as num_active FROM tb_analysis  " +
            " WHERE record_date >= #{startDate} AND record_date <= #{endDate}\n")
    Integer selectnum_active(@Param("startDate") DateTime startDate,@Param("endDate")DateTime endDate);

    /**
     * 查询去年的活跃数
     * @param startDate
     * @param endDate
     * @return
     */
    @Select("select  sum(num_active) as num_active from  tb_analysis where record_date >= DATE_ADD(#{startDate}, INTERVAL -1 YEAR)" +
            "AND record_date <= DATE_ADD(#{endDate}, INTERVAL -1 YEAR)")
    Integer selectnum_activetoyear(@Param("startDate") DateTime startDate,@Param("endDate")DateTime endDate);

    /**
     * 查询本年的次日留存率
     * @param startDate
     * @param endDate
     * @return
     */
    @Select("SELECT   sum(num_retention1d) as num_retention1d FROM tb_analysis  " +
            " WHERE record_date >= #{startDate} AND record_date <= #{endDate}\n")
    Integer selectnum_retention1d(@Param("startDate") DateTime startDate,@Param("endDate")DateTime endDate);


    /**
     * 查询去年的次日留存率
     * @param startDate
     * @param endDate
     * @return
     */
    @Select("select  sum(num_retention1d) as num_retention1d from  tb_analysis where record_date >= DATE_ADD(#{startDate}, INTERVAL -1 YEAR)" +
            "AND record_date <= DATE_ADD(#{endDate}, INTERVAL -1 YEAR)")
    Integer selectnum_retention1dtoyear(DateTime startDate, DateTime endDate);
}
