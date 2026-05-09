package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_activity") // 对应数据库表名
public class BizActivity {

    @TableId(type = IdType.AUTO) // 主键自增
    private Long id;

    private String title;
    private String content;
    private String coverImage;

    //活动分类
    private String category;
    
    //活动名额上限
    private Integer capacity;
    
    //当前已报名人数
    @TableField(exist = false)
    private Integer currentEnrollment;
    //处理时间格式
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    private String location;
    private Long publisherId;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}