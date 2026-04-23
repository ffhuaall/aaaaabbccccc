package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_lost_found_comment")
public class BizLostFoundComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long itemId;      // 对应的物品ID
    private Long userId;      // 留言人用户ID
    private String content;    // 留言内容
    private LocalDateTime createTime; // 留言时间
}