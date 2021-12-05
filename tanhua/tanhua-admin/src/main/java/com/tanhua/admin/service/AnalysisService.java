package com.tanhua.admin.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.admin.mapper.AnalysisMapper;
import com.tanhua.admin.mapper.LogMapper;
import domain.Analysis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vo.AnalysisUsersVo;
import vo.DataPointVo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AnalysisService {

    @Autowired
    private LogMapper logMapper;
    @Autowired
    private AnalysisMapper analysisMapper;
    //定时去统计log表中的数据,保存或者更新tb_analysis数据
    //查询tb_log表(注册用户数,登录用户数,活跃用户数,次日留存)
    //构造analysis对象
    //保存或者更新
    public void analysis() throws ParseException {
    //定义查询日期
        String todayStr=new SimpleDateFormat("yyyy-MM-dd").format(new Date());
     //借助hutu工具类定义昨日日期
        String yestodayStr = DateUtil.yesterday().toString("yyyy-MM-dd");
        //统计数据-注册数量
        Integer regCount = logMapper.queryByTypeAndLogTime("0102",todayStr);
        //统计数量-登录数量
        Integer loginCount = logMapper.queryByTypeAndLogTime("0101",todayStr);
        //统计数量-活跃数量
        Integer activeCount = logMapper.queryByLogTime(todayStr);
        //统计数量-次日留存
        Integer numRetention1d = logMapper.queryNumRetention1d(todayStr, yestodayStr);
        //构造Analysis对象
        QueryWrapper<Analysis> qw = new QueryWrapper<>();
        //数据库要的date类型,所以要把字符串转换为时间类型
        //根据日期查询数据
        qw.eq("record_date",new SimpleDateFormat("yyyy-MM-dd").parse(todayStr));
        Analysis analysis = analysisMapper.selectOne(qw);
        //如果存在,更新,不存在保存
        if(analysis!=null){
            //7、如果存在，更新
            analysis.setNumRegistered(regCount);
            analysis.setNumLogin(loginCount);
            analysis.setNumActive(activeCount);
            analysis.setNumRetention1d(numRetention1d);
            analysis.setUpdated(new Date());
            analysisMapper.updateById(analysis);
        }else {
            //8、如果不存在，保存
            analysis = new Analysis();
            analysis.setNumRegistered(regCount);
            analysis.setNumLogin(loginCount);
            analysis.setNumActive(activeCount);
            analysis.setNumRetention1d(numRetention1d);
            analysis.setRecordDate(new SimpleDateFormat("yyyy-MM-dd").parse(todayStr));
            analysis.setCreated(new Date());
            analysis.setUpdated(null);
            analysisMapper.insert(analysis);
        }
    }


    /**
     * 查询同一时间内去年和今年的数据
     * @param sd 开始时间
     * @param ed 结束时间
     * @param type 类型
     *             101 新增 102 活跃用户 103 次日留存率
     * @return
     */
    public AnalysisUsersVo queryAnalysisUsersVo(Long sd, Long ed, Integer type) {
        //转换为日期类型
        DateTime startDate = DateUtil.date(sd);
        DateTime endDate = DateUtil.date(ed);
        //查询本年的
        AnalysisUsersVo analysisUsersVo=new AnalysisUsersVo();
        //保存今年的数据
        List<DataPointVo> list=new ArrayList<>();
        //保存去年的数据
        List<DataPointVo> lastlist=new ArrayList<>();
        switch (type){
            case 101: {
                //调用方法查询今年的注册数和去年的注册数
                Integer sum = analysisMapper.selectnum_registered(startDate, endDate);
                DataPointVo dataPointVo = new DataPointVo();
                dataPointVo.setTitle("num_registered");
                dataPointVo.setAmount(sum.longValue());
                list.add(dataPointVo);
                //查询去年这个日期的注册数
                Integer sumyear = analysisMapper.selectnum_registeredtoyear(startDate, endDate);
                dataPointVo.setTitle("num_registered");
                dataPointVo.setAmount(sum.longValue());
                lastlist.add(dataPointVo);
                break;
            }
            case 102:{
                //查询本年和去年活跃用户数
                Integer sum = analysisMapper.selectnum_active(startDate, endDate);
                DataPointVo dataPointVo = new DataPointVo();
                dataPointVo.setTitle("num_active");
                dataPointVo.setAmount(sum.longValue());
                list.add(dataPointVo);
                //查询去年这个日期的注册数
                Integer sumyear = analysisMapper.selectnum_activetoyear(startDate, endDate);
                dataPointVo.setTitle("num_active");
                dataPointVo.setAmount(sum.longValue());
                lastlist.add(dataPointVo);
                break;
            }
            case 103:{
                //查询本年的次日留存率
                Integer sum = analysisMapper.selectnum_retention1d(startDate, endDate);
                DataPointVo dataPointVo = new DataPointVo();
                dataPointVo.setTitle("num_retention1d");
                dataPointVo.setAmount(sum.longValue());
                list.add(dataPointVo);
                //查询去年的次日留存率
                Integer sumyear = analysisMapper.selectnum_retention1dtoyear(startDate, endDate);
                dataPointVo.setTitle("num_retention1d");
                dataPointVo.setAmount(sum.longValue());
                lastlist.add(dataPointVo);
                break;
            }
        }
        analysisUsersVo.setThisYear(list);
        analysisUsersVo.setLastYear(lastlist);
        return analysisUsersVo;



    }
}
