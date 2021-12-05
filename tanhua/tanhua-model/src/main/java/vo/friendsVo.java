package vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 互相喜欢,喜欢,粉丝列表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class friendsVo {
    private Long id; //用户id
    private String avatar;//头像
    private String nickname; //昵称
    private String gender; //性别 man woman
    private Integer age;//年龄
    private String education;//城市
    private Integer marriage;//婚姻状态（0未婚，1已婚）
    private Integer matchRate;//匹配度
    private boolean alreadyLove;//是否喜欢ta





}
