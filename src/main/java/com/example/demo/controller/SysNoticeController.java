package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.entity.SysNotice; // 需自行创建 Entity
import com.example.demo.mapper.SysNoticeMapper; // 需自行创建 Mapper
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notice")
public class SysNoticeController {

    @Autowired
    private SysNoticeMapper noticeMapper;

    // 超管：发布/修改公告
    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody SysNotice notice) {
        if (notice.getId() == null) {
            notice.setCreateTime(java.time.LocalDateTime.now());
        }
        if (notice.getIsActive() == 1) {
            // 确保全站同时只有一个激活的强提醒公告（可选逻辑）
            SysNotice update = new SysNotice();
            update.setIsActive(0);
            noticeMapper.update(update, new QueryWrapper<SysNotice>().eq("is_active", 1));
        }
        // 这里为了简单，支持多条发布。如果用 saveOrUpdate 需配置 Service
        if (notice.getId() == null) noticeMapper.insert(notice);
        else noticeMapper.updateById(notice);
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