package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.BizActivity;
import com.example.demo.service.BizActivityService;
import com.example.demo.service.SysUserService;
import com.example.demo.entity.BizActivityRegistration;
import com.example.demo.entity.SysMessage;
import com.example.demo.entity.SysUser;

import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.entity.BizActivityRegistration;
import com.example.demo.mapper.BizActivityRegistrationMapper;
import com.example.demo.mapper.SysMessageMapper;

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
    @Autowired
    private SysMessageMapper messageMapper;
    /**
     * 查询所有活动列表
     */
    @GetMapping("/list")
    public Result<List<BizActivity>> list() {
        List<BizActivity> list = activityService.list();
        
        // 遍历活动，动态统计每个活动的已报名人数
        for (BizActivity activity : list) {
            QueryWrapper<BizActivityRegistration> countWrapper = new QueryWrapper<>();
            countWrapper.eq("activity_id", activity.getId()).eq("status", 1);
            long count = registrationMapper.selectCount(countWrapper);
            activity.setCurrentEnrollment((int) count);
        }
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
    @PostMapping("/enroll")
    public Result<Boolean> enrollActivity(@RequestParam Long activityId, @RequestParam Long userId) {
        // 1. 检查活动是否存在及状态
        BizActivity activity = activityService.getById(activityId);
        if (activity == null || activity.getStatus() != 1) {
            return Result.error(400, "活动不存在或不在报名状态");
        }

        // 2. 检查名额是否已满 (如果有 capacity 限制的话)
        if (activity.getCapacity() != null && activity.getCapacity() > 0) {
            QueryWrapper<BizActivityRegistration> countWrapper = new QueryWrapper<>();
            countWrapper.eq("activity_id", activityId).eq("status", 1);
            long currentCount = registrationMapper.selectCount(countWrapper);
            if (currentCount >= activity.getCapacity()) {
                return Result.error(400, "手慢了，该活动名额已满！");
            }
        }

        // 3. 检查用户是否已报名
        QueryWrapper<BizActivityRegistration> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId).eq("user_id", userId);
        BizActivityRegistration existingReg = registrationMapper.selectOne(wrapper);

        if (existingReg != null) {
            if (existingReg.getStatus() == 1) {
                return Result.error(400, "您已经报名过该活动啦！");
            }
            // 之前取消过，现在重新报名
            existingReg.setStatus(1);
            existingReg.setCreateTime(LocalDateTime.now());
            registrationMapper.updateById(existingReg);
            return Result.success(true);
        }

        // 4. 首次报名
        BizActivityRegistration reg = new BizActivityRegistration();
        reg.setActivityId(activityId);
        reg.setUserId(userId);
        reg.setStatus(1);
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
     * 【新增】取消报名接口
     */
    @PostMapping("/cancel-enroll")
    public Result<Boolean> cancelEnroll(@RequestParam Long activityId, @RequestParam Long userId) {
        QueryWrapper<BizActivityRegistration> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId).eq("user_id", userId).eq("status", 1);
        BizActivityRegistration reg = registrationMapper.selectOne(wrapper);
        
        if (reg != null) {
            reg.setStatus(0); // 0代表已取消
            registrationMapper.updateById(reg);
            return Result.success(true);
        }
        return Result.error(400, "您未报名该活动或已被取消");
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

    //停止报名
    @PostMapping("/stop/{id}")
    public Result<Boolean> stopActivity(@PathVariable Long id) {
        BizActivity activity = activityService.getById(id);
        if (activity != null) {
            activity.setStatus(0); // 0-已结束
            activityService.updateById(activity);

            // 【闭环修复】给活动发布者发送通知
            SysMessage msg = new SysMessage();
            msg.setReceiverId(activity.getPublisherId());
            msg.setTitle("活动状态变更通知");
            msg.setContent("您好！您发布的校园活动【" + activity.getTitle() + "】已被管理员强制提前结束报名。");
            msg.setType("ACTIVITY");
            msg.setIsRead(0);
            msg.setCreateTime(LocalDateTime.now());
            messageMapper.insert(msg);

            return Result.success(true);
        }
        return Result.error(400, "活动不存在");
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

            // 【新增逻辑】如果劝退，给学生发实时通知
            if (status == 0) {
                SysMessage msg = new SysMessage();
                // 【关键修改】使用 setReceiverId
                msg.setReceiverId(reg.getUserId()); 
                msg.setTitle("活动资格变动通知");
                msg.setContent("遗憾通知：您报名的活动状态已被管理员变更为 [已取消]，请前往活动中心查看详情。");
                msg.setType("ACTIVITY");
                msg.setIsRead(0);
                msg.setCreateTime(java.time.LocalDateTime.now());
                messageMapper.insert(msg);
            }
            return Result.success(true);
        }
        return Result.error(400, "记录不存在");
    }

    /**
     * 【超管专属/负责人通用】物理删除活动及其关联的报名记录
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteActivity(@PathVariable Long id) {
        // 0. 先查出活动信息（为了拿到标题和发布者ID发通知，否则删完就查不到了）
        BizActivity activity = activityService.getById(id);
        if (activity == null) {
            return Result.error(400, "操作失败：活动不存在");
        }

        // 1. 删除活动主表记录
        activityService.removeById(id);
        
        // 2. 清理相关的报名记录 (级联删除，防止产生脏数据)
        QueryWrapper<BizActivityRegistration> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", id);
        registrationMapper.delete(wrapper);

        // 3. 【闭环修复】给活动发布者发送违规下架/删除通知
        SysMessage msg = new SysMessage();
        msg.setReceiverId(activity.getPublisherId());
        msg.setTitle("系统管理通知：活动已被强制删除");
        msg.setContent("您好！您发布的校园活动【" + activity.getTitle() + "】由于违规或其他原因，已被超级管理员强制从系统中删除，相关报名数据已作废。");
        msg.setType("ACTIVITY");
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(msg);
        
        return Result.success(true);
    }
    
}