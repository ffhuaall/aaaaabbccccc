package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.entity.SysUser;
import com.example.demo.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserService userService;

    /**
     * 1. 获取全校用户列表 (支持根据学号/姓名模糊搜索)
     */
    @GetMapping("/list")
    public Result<List<SysUser>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer roleId) { // [新增] 角色筛选参数
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        
        // 如果传了角色ID，则进行精确匹配
        if (roleId != null) {
            wrapper.eq("role_id", roleId);
        }
        
        // 关键词模糊匹配
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like("username", keyword).or().like("real_name", keyword));
        }
        
        wrapper.orderByDesc("create_time");
        return Result.success(userService.list(wrapper));
    }

    /**
     * 2. 新增或修改用户
     */
    @PostMapping("/save")
    public Result<Boolean> saveUser(@RequestBody SysUser user) {
        if (user.getId() == null) {
            // 新增账号时，给个初始默认密码：123456
            user.setPassword("$2a$10$TxNe8O5NA7j2tLbXbTGGfu2u9t.AFYqrvs4QWyNdhZl95");
            user.setStatus(1); // 默认状态正常
            user.setCreateTime(java.time.LocalDateTime.now());
        }
        return Result.success(userService.saveOrUpdate(user));
    }

    /**
     * 3. 封禁/解封账号 (状态 1正常，0禁用)
     */
    @PostMapping("/status")
    public Result<Boolean> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        return Result.success(userService.updateById(user));
    }

    /**
     * 4. 重置密码为 123456 (专门拯救忘带脑子的同学)
     */
    @PostMapping("/reset-pwd/{id}")
    public Result<Boolean> resetPwd(@PathVariable Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword("$2a$10$TxNe8O5NA7j2tLbXbTGGfu2u9t.AFYqrvs4QWyNdhZl95");
        return Result.success(userService.updateById(user));
    }

    /**
     * 5. [新增] 用户修改个人资料 (仅限修改非敏感字段)
     */
    @PostMapping("/update-profile")
    public Result<Boolean> updateProfile(@RequestBody SysUser user) {
        // 安全限制：个人中心只允许修改手机号、真实姓名、头像
        // 学号、角色、状态等核心属性通过 ID 关联更新，但逻辑上由超管在 save 接口控制
        SysUser updateData = new SysUser();
        updateData.setId(user.getId());
        updateData.setRealName(user.getRealName());
        updateData.setPhone(user.getPhone());
        updateData.setAvatar(user.getAvatar());
        
        return Result.success(userService.updateById(updateData));
    }
}