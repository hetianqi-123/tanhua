package com.tanhua.dubbo.api;

import api.QuestionApi;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.dubbo.mappers.QuestionMapper;
import domain.Question;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

//暴露服务
@DubboService
public class QuestionApiImpl implements QuestionApi {
    @Autowired
    private QuestionMapper questionMapper;
    /**
     * 获取陌生人问题
     * @param userId
     */
    @Override
    public Question findByUserId(Long userId) {
        QueryWrapper<Question> qw=new QueryWrapper<>();
        //根据传入的id查询出问题内容
        qw.eq("user_id",userId);
        Question question = questionMapper.selectOne(qw);
        return question;
    }

    /**
     * 如果查询没有陌生人问题，就进行保存
     */
    @Override
    public void saveSettings(Question question) {
        questionMapper.insert(question);

    }


    /**
     * 如果查询已经有陌生人问题，就进行更新替换
     */
    @Override
    public void updateSettings(Question question) {
        questionMapper.updateById(question);
    }


}
