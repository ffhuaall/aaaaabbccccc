package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.entity.BizRepairOrder;
import com.example.demo.mapper.BizActivityMapper;
import com.example.demo.service.BizRepairOrderService;
import com.example.demo.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private SysUserService userService;

    @Autowired
    private BizRepairOrderService repairOrderService;

    @Autowired
    private BizActivityMapper activityMapper;

    /**
     * 获取数据大屏所需的所有综合统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> dashboardData = new HashMap<>();

        // 1. 统计基础数据：平台总用户数
        long totalUsers = userService.count();
        dashboardData.put("totalUsers", totalUsers);

        // 2. 统计后勤报修数据：总工单数 vs 已完成工单数 (状态为2待评价或3已完成，都算维修结束)
        long totalRepairs = repairOrderService.count();
        QueryWrapper<BizRepairOrder> repairQuery = new QueryWrapper<>();
        repairQuery.in("status", 2, 3);
        long completedRepairs = repairOrderService.count(repairQuery);
        
        Map<String, Long> repairStats = new HashMap<>();
        repairStats.put("total", totalRepairs);
        repairStats.put("completed", completedRepairs);
        dashboardData.put("repairStats", repairStats);

        // 3. 统计活动热度：获取 Top 5 热门活动
        List<Map<String, Object>> topActivities = activityMapper.getTopPopularActivities();
        dashboardData.put("topActivities", topActivities);

        return Result.success(dashboardData);
    }
}