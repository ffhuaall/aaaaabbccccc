package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_log")
public class SysLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 操作人账号/姓名
    private String username;
    
    // 操作模块
    private String module;
    
    // 动作简述
    private String action;
    
    // 标签类型 (success/warning/danger/info)
    private String type;
    
    // 详细描述
    private String detail;
    
    // 创建时间
    private LocalDateTime createTime;
}