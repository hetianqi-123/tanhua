package domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * 实现设置会写的vo类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettingsVo implements Serializable {

    private Long id;
    //陌生消息
    private String strangerQuestion = "";
    private String phone;
    private Boolean likeNotification = true;
    private Boolean pinglunNotification = true;
    private Boolean gonggaoNotification = true;

}