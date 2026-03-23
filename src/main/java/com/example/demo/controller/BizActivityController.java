package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.BizActivity;
import com.example.demo.service.BizActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/activity")
public class BizActivityController {

    @Autowired
    private BizActivityService activityService;

    /**
     * 查询所有活动列表
     */
    @GetMapping("/list")
    public Result<List<BizActivity>> list() {
        List<BizActivity> list = activityService.list();
        return Result.success(list);
    }

    /**
     * 发布/新增一个活动 (测试用)
     */
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody BizActivity activity) {
        // 设置默认值
        if (activity.getCreateTime() == null) {
            activity.setCreateTime(LocalDateTime.now());
        }
        boolean success = activityService.save(activity);
        return Result.success(success);
    }
}