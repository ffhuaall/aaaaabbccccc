package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统公告实体类
 */
@Data
@TableName("sys_notice")
public class SysNotice {
    
    // bigint(20) 对应 Long
    @TableId(type = IdType.AUTO)
    private Long id;

    // varchar(100) 对应 String
    private String title;

    // text 对应 String
    private String content;

    // varchar(20) 对应 String (存储 info, warning, error 等前端所需类型)
    private String level;

    // tinyint(1) 对应 Integer (1代表发布中，0代表已下线或草稿)
    private Integer isActive;

    // datetime 对应 Java 8 的 LocalDateTime
    private LocalDateTime createTime;
}