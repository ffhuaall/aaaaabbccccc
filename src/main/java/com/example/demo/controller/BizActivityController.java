package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.BizActivity;
import com.example.demo.service.BizActivityService;
import com.example.demo.service.SysUserService;
import com.example.demo.entity.BizActivityRegistration;
import com.example.demo.entity.SysUser;

import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.entity.BizActivityRegistration;
import com.example.demo.mapper.BizActivityRegistrationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/activity")
public class BizActivityController {

    @Autowired
    private BizActivityService activityService;
    @Autowired
    private BizActivityRegistrationMapper registrationMapper;
    @Autowired
    private SysUserService userService;
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
    // @PostMapping("/add")
    // public Result<Boolean> add(@RequestBody BizActivity activity) {
    //     // 设置默认值
    //     if (activity.getCreateTime() == null) {
    //         activity.setCreateTime(LocalDateTime.now());
    //     }
    //     boolean success = activityService.save(activity);
    //     return Result.success(success);
    // }

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

    /**
     * 【新增】发布新活动
     */
    @PostMapping("/add")
    public Result<Boolean> addActivity(@RequestBody BizActivity activity) {
        activity.setStatus(1); // 默认发布即为“报名中”
        activity.setCreateTime(java.time.LocalDateTime.now());
        // 实际开发中，publisherId 应该从当前登录的 Token 中获取
        return Result.success(activityService.save(activity));
    }

    /**
     * 【新增】获取某个活动的报名人员列表 (带学生姓名)
     * 这里我们用一个 Map 来简单承载关联数据
     */
    @GetMapping("/participants/{activityId}")
    public Result<List<Map<String, Object>>> getParticipants(@PathVariable Long activityId) {
        // 1. 先查报名表
        QueryWrapper<BizActivityRegistration> regWrapper = new QueryWrapper<>();
        regWrapper.eq("activity_id", activityId);
        List<BizActivityRegistration> regs = registrationMapper.selectList(regWrapper);
        
        if (regs.isEmpty()) return Result.success(new ArrayList<>());

        // 2. 关联查询学生姓名 (这里为了演示简单，我们循环查。实际项目中建议用 SQL Join)
        List<Map<String, Object>> result = regs.stream().map(reg -> {
            Map<String, Object> map = new HashMap<>();
            map.put("regId", reg.getId());
            map.put("userId", reg.getUserId());
            map.put("status", reg.getStatus());
            map.put("createTime", reg.getCreateTime());
            
            // 查用户信息
            SysUser user = userService.getById(reg.getUserId());
            map.put("realName", user != null ? user.getRealName() : "未知学生");
            map.put("username", user != null ? user.getUsername() : "---");
            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    /**
     * 【新增】审核/操作报名人员状态 (如：取消资格/签到等)
     */
    @PostMapping("/audit-participant")
    public Result<Boolean> auditParticipant(@RequestParam Long regId, @RequestParam Integer status) {
        BizActivityRegistration reg = registrationMapper.selectById(regId);
        if (reg != null) {
            reg.setStatus(status);
            registrationMapper.updateById(reg);
            return Result.success(true);
        }
        return Result.error(400, "记录不存在");
    }
}