package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("biz_classroom")
public class BizClassroom {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String campus;    // 校区
    private String building;  // 教学楼
    private String roomNo;    // 教室号
    private Integer capacity; // 容量
}