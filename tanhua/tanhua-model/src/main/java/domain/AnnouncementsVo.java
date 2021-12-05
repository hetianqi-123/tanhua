package domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor  //满参构造方法
@NoArgsConstructor   //无参构造方法
public class AnnouncementsVo implements Serializable {
    private String id;
    private String title;
    private String description;
    private  String createDate;
}
