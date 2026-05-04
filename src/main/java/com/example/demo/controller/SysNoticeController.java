package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.entity.SysLog;
import com.example.demo.entity.SysNotice;
import com.example.demo.mapper.SysLogMapper;
import com.example.demo.mapper.SysNoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notice")
public class SysNoticeController {

    @Autowired
    private SysNoticeMapper noticeMapper;

    // 【新增】注入日志 Mapper
    @Autowired
    private SysLogMapper logMapper;

    // 超管：发布/修改公告
    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody SysNotice notice) {
        boolean isNew = (notice.getId() == null);

        if (isNew) {
            notice.setCreateTime(java.time.LocalDateTime.now());
        }
        if (notice.getIsActive() != null && notice.getIsActive() == 1) {
            SysNotice update = new SysNotice();
            update.setIsActive(0);
            noticeMapper.update(update, new QueryWrapper<SysNotice>().eq("is_active", 1));
        }

        if (isNew) {
            noticeMapper.insert(notice);
            // 🌟 【风控埋点】记录发布公告操作
            SysLog log = new SysLog();
            log.setUsername("superadmin"); // 实际可从Token提取
            log.setModule("系统公告");
            log.setAction("发布公告");
            log.setType("info");
            log.setDetail("发布了新的系统公告：【" + notice.getTitle() + "】");
            logMapper.insert(log);
        } else {
            noticeMapper.updateById(notice);
        }

        return Result.success(true);
    }

    // 全员：获取当前正在发布的最新公告
    @GetMapping("/latest")
    public Result<SysNotice> getLatest() {
        QueryWrapper<SysNotice> wrapper = new QueryWrapper<>();
        wrapper.eq("is_active", 1).orderByDesc("create_time").last("limit 1");
        return Result.success(noticeMapper.selectOne(wrapper));
    }

    // 超管：获取所有历史公告列表
    @GetMapping("/list")
    public Result<List<SysNotice>> list() {
        return Result.success(noticeMapper.selectList(new QueryWrapper<SysNotice>().orderByDesc("create_time")));
    }
}