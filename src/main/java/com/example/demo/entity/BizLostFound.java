package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_lost_found")
public class BizLostFound {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer type; // 0-失物寻物, 1-捡到招领
    private String itemName; // 物品名称
    private String description; // 详细描述
    private String location; // 地点
    private String contactInfo; // 联系方式
    private String images; // JSON 字符串格式的图片
    private Long publisherId;
    private Integer status; // 0-寻找中, 1-已解决, 2-违规下架
    private LocalDateTime createTime;
}