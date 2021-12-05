package vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceVo {

    private Long id;
    private String avatar; //头像
    private String nickname; //昵称
    private String gender; //性别
    private int age; //年龄
    private String soundUrl; //声音地址
    private int remainingTimes; //剩余次数
}