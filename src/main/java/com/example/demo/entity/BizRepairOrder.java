package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_repair_order")
public class BizRepairOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long workerId;
    private String dormLocation;
    private String title;
    private String description;
    private String images; 
    private Integer status; // 0-待接单, 1-维修中, 2-待评价, 3-已完成
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}