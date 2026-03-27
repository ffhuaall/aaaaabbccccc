package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_message")
public class SysMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long receiverId;
    private String title;
    private String content;
    private Integer isRead; // 0未读 1已读
    private String type; // REPAIR, ACTIVITY, SYSTEM
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}