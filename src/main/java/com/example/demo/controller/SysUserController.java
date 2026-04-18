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
    public Result<List<SysUser>> getList(@RequestParam(required = false) String keyword) {
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            // 模糊匹配学号或真实姓名
            wrapper.like("username", keyword).or().like("real_name", keyword);
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
            user.setPassword("123456");
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
        user.setPassword("123456");
        return Result.success(userService.updateById(user));
    }
}