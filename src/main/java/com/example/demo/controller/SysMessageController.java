package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.entity.SysMessage;
import com.example.demo.mapper.SysMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
public class SysMessageController {

    @Autowired
    private SysMessageMapper messageMapper;

    //获取我的所有消息
    @GetMapping("/list")
    public Result<List<SysMessage>> getMyMessages(@RequestParam Long userId) {
        QueryWrapper<SysMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", userId).orderByDesc("create_time");
        return Result.success(messageMapper.selectList(wrapper));
    }

    //获取未读消息数量
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(@RequestParam Long userId) {
        QueryWrapper<SysMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", userId).eq("is_read", 0);
        return Result.success(messageMapper.selectCount(wrapper));
    }

    //将单条消息标为已读
    @PostMapping("/read/{id}")
    public Result<Boolean> readMessage(@PathVariable Long id) {
        SysMessage msg = messageMapper.selectById(id);
        if (msg != null) {
            msg.setIsRead(1);
            messageMapper.updateById(msg);
        }
        return Result.success(true);
    }
    
    //一键已读所有
    @PostMapping("/read-all")
    public Result<Boolean> readAll(@RequestParam Long userId) {
        SysMessage msg = new SysMessage();
        msg.setIsRead(1);
        QueryWrapper<SysMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", userId).eq("is_read", 0);
        messageMapper.update(msg, wrapper);
        return Result.success(true);
    }
}