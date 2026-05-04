package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.entity.SysLog;
import com.example.demo.mapper.SysLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/log")
public class SysLogController {

    @Autowired
    private SysLogMapper logMapper;

    /**
     * 供超管大屏展示：拉取全站最新的 6 条敏感操作日志
     */
    @GetMapping("/recent")
    public Result<List<SysLog>> getRecentLogs() {
        QueryWrapper<SysLog> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time").last("limit 6"); 
        return Result.success(logMapper.selectList(wrapper));
    }
}