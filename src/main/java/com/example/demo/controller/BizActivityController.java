package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.BizActivity;
import com.example.demo.service.BizActivityService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.entity.BizActivityRegistration;
import com.example.demo.mapper.BizActivityRegistrationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/activity")
public class BizActivityController {

    @Autowired
    private BizActivityService activityService;
    @Autowired
    private BizActivityRegistrationMapper registrationMapper;

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

    /**
     * 个性化活动推荐接口 (基于用户的协同过滤)
     * 示例: GET /activity/recommend?userId=1002&topN=3
     */
    @GetMapping("/recommend")
    public Result<List<BizActivity>> recommend(@RequestParam Long userId, 
                                               @RequestParam(defaultValue = "3") Integer topN) {
        List<BizActivity> recommendedList = activityService.recommendActivities(userId, topN);
        return Result.success(recommendedList);
    }

    //活动报名接口
    @PostMapping("/register")
    public Result<Boolean> registerActivity(@RequestParam Long activityId, @RequestParam Long userId) {
        // 1. 防重复报名校验：查询数据库里是不是已经有这条记录了
        QueryWrapper<BizActivityRegistration> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId).eq("user_id", userId);
        if (registrationMapper.selectCount(wrapper) > 0) {
            return Result.error(400, "您已经报名过该活动啦，请勿重复操作！");
        }

        // 2. 插入报名记录
        BizActivityRegistration reg = new BizActivityRegistration();
        reg.setActivityId(activityId);
        reg.setUserId(userId);
        reg.setStatus(1); // 1 代表正常报名状态
        reg.setCreateTime(LocalDateTime.now());
        registrationMapper.insert(reg);

        return Result.success(true);
    }
    //获取当前用户已报名的活动 ID 列表
    @GetMapping("/my-registered")
    public Result<List<Long>> getMyRegisteredActivities(@RequestParam Long userId) {
        QueryWrapper<BizActivityRegistration> wrapper = new QueryWrapper<>();
        // 查询该用户所有状态为 1 (已报名) 的记录
        wrapper.eq("user_id", userId).eq("status", 1);
        List<BizActivityRegistration> list = registrationMapper.selectList(wrapper);
        
        // 使用 Java 8 Stream 提取出所有的 activityId
        List<Long> activityIds = list.stream()
                .map(BizActivityRegistration::getActivityId)
                .collect(Collectors.toList());
                
        return Result.success(activityIds);
    }
}