package yuxuan.travelassisant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("scenic_spot")
public class ScenicSpot {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer volume;

    private String freeTime;

    private String rule;
}