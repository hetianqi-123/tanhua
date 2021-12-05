package api;

import domain.Question;

public interface QuestionApi {
    /**
     * 读取陌生人问题
     */
    public Question findByUserId(Long userId);


    /**
     * 如果查询没有陌生人问题，就进行保存
     */
    void saveSettings(Question question);

    /**
     * 如果查询已经有陌生人问题，就进行更新替换
     */
    void updateSettings(Question question);
}
