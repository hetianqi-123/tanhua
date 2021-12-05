package com.tanhua.server.service;

import api.UserInfoApi;
import api.peachblossomApi;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import domain.UserInfo;
import mongo.Voice;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vo.ErrorResult;
import vo.VoiceVo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class PeachblossomService {

    //上传fastDFS对象
    @Autowired
    private FastFileStorageClient clientc;
    //访问地址
    @Autowired
    private FdfsWebServer webServer;

    @DubboReference
    private UserInfoApi userInfoApi;

    @DubboReference
    private peachblossomApi peachblossomApi;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 桃花传音-发送语音
     * @param soundFile 发送的语音文件
     */
    public void ChuanYin(MultipartFile soundFile) throws IOException {
        //判断语音是否为空
        if(soundFile.isEmpty()){
            //为空抛出异常
            throw new BusinessException(ErrorResult.error());
        }

        //将语音上传到fastDFS
        //获取到语音名称
        String originalFilename = soundFile.getOriginalFilename();
        //先获取最后一位.的索引+1,然后进行截取,获取到后缀名
        originalFilename= originalFilename.substring(originalFilename.lastIndexOf(".")+1);
        //保存到fastDFS中
        StorePath path = clientc.uploadFile(soundFile.getInputStream(), soundFile.getSize(), originalFilename, null);
        //获取到绝对路径
        String fullPath = path.getFullPath();
        //获取到Nginx反向代理的访问地址
        String voiceurl =webServer.getWebServerUrl()+fullPath;
        //构造语音对象Voice
        Voice voice=new Voice();
        voice.setUserId(UserHolder.getUserId());
        //获取用户详情
        UserInfo userInfo = userInfoApi.findById(UserHolder.getUserId());
        //头像
        voice.setAvatar(userInfo.getAvatar());
        //昵称
        voice.setNickname(userInfo.getNickname());
        //性别
        voice.setGender(userInfo.getGender());
        //年龄
        voice.setAge(userInfo.getAge());
        //声音连接
        voice.setSoundUrl(voiceurl);
        //时间
        voice.setDate(System.currentTimeMillis());
        //保存到mongodb中
      String id= peachblossomApi.saveVoice(voice);
      if(StringUtils.isEmpty(id)){
          //如果为空,保存失败
          throw new BusinessException(ErrorResult.error());
      }
    }



    /**
     * 接收语音
     * @return
     */
    public VoiceVo JieShou() {
        //获取到当前时间
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        String format = simpleDateFormat.format(new Date());
        //判断用户是否已经获取了10条语音
        String s = redisTemplate.opsForValue().get(format + UserHolder.getUserId().toString());
        Integer num=0;
       // Voice voice=null;
        if(s!=null){
             num =Integer.valueOf(redisTemplate.opsForValue().get(format + UserHolder.getUserId().toString())) ;
            if(num>=10 ){
                throw new BusinessException(ErrorResult.error());
            }
        }
            //随机获取语音
        Voice  voice = this.suijiyuyin();
            //保存到redis中
            //拼接key
            //key就是用户的id+当前时间
            String key =format+UserHolder.getUserId();
            //value就是记录数
            int value=++num;
            //保存到redis中
            redisTemplate.opsForValue().set(key,String.valueOf(value));

        //构造VoiceVo对象
        VoiceVo voiceVo=new VoiceVo();
        voiceVo.setAvatar(voice.getAvatar());
        voiceVo.setNickname(voice.getNickname());
        voiceVo.setGender(voice.getGender());
        voiceVo.setAge(voice.getAge());
        voiceVo.setSoundUrl(voice.getSoundUrl());
        voiceVo.setRemainingTimes(10-num);
        //判断是否存在语音
        if(voice==null){
            throw new BusinessException(ErrorResult.error());
        }
        //如果不为空,进行删除
        peachblossomApi.deleteyuyin(voice);
        return voiceVo;
    }

    //首先随机获取语音
    public Voice suijiyuyin(){
        //获取到随机的语音
        return peachblossomApi.suiji(UserHolder.getUserId());
    }









}
