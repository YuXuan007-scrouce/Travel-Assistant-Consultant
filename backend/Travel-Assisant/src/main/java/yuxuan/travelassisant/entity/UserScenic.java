package yuxuan.travelassisant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("user_scenic")
public class UserScenic {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long scenicId;

    private String scenicName;

    private LocalDate date;

    private String timeSlot;

    private Integer personCount;

    private String contactName;

    private String phone;

    private Long userId;

    private String orderNo;
}