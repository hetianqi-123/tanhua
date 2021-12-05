package api;


import mongo.Voice;

public interface peachblossomApi {
    /**
     * 桃花传音-发送语音
     * @param voice
     * @return
     */
    String saveVoice(Voice voice);


    /**
     * 随机获取语音
     * @param userId
     * @return
     */
    Voice suiji(Long userId);

    /**
     * 删除语音
     * @param voice
     */
    void deleteyuyin(Voice voice);
}
