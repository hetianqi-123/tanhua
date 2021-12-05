package domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor  //满参构造方法
@NoArgsConstructor   //无参构造方法
public class Announcements implements Serializable {
    //编号id
    private String id;
    //公告抬头
    private String title;
    //公告内容
    private String description;
    //公告日期
   private String created;
//    @TableField(value = "created")
//    private String createDate;




}
