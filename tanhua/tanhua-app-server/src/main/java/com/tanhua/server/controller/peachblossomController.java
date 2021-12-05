package com.tanhua.server.controller;

import com.tanhua.server.service.PeachblossomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vo.VoiceVo;

import java.io.IOException;

@RestController
@RequestMapping("peachblossom")
public class peachblossomController {

    @Autowired
    private PeachblossomService peachblossomService;

    /**
     * 桃花传音-发送语音
     * @param soundFile 发送的语音文件
     * @return
     */
    @PostMapping
    public ResponseEntity ChuanYin(MultipartFile soundFile) throws IOException {
        peachblossomService.ChuanYin(soundFile);
        return  ResponseEntity.ok(null);

    }


    /**
     * 接收语音
     * @return
     */
    @GetMapping
    public ResponseEntity JieShou(){
        VoiceVo voiceVo=  peachblossomService.JieShou();
        return  ResponseEntity.ok(voiceVo);
    }

}
