package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.entity.SysLog;
import com.example.demo.entity.SysUser;
import com.example.demo.mapper.SysLogMapper;
import com.example.demo.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserService userService;

    // 【新增】直接注入 SecurityConfig 中配置好的密码加密器
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SysLogMapper logMapper;

    /**
     * 1. 获取全校用户列表 (支持根据学号/姓名模糊搜索)
     */
    @GetMapping("/list")
    public Result<List<SysUser>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer roleId) { 
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
            // 新增账号时，如果有传密码就用传的，没传默认给 123456，然后动态加密！
            String rawPwd = StringUtils.hasText(user.getPassword()) ? user.getPassword() : "123456";
            user.setPassword(passwordEncoder.encode(rawPwd));
            
            user.setStatus(1); // 默认状态正常
            user.setCreateTime(java.time.LocalDateTime.now());
        } else {
            // 如果是修改账号信息，且前端又传了新密码过来，也要记得加密
            if (StringUtils.hasText(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
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
        boolean res = userService.updateById(user);

        // 🌟 【风控埋点】记录账号的封禁与解封
        SysLog log = new SysLog();
        log.setUsername("superadmin"); // 实际可从Token/Session中获取当前登录者
        log.setModule("用户管理");
        log.setAction(status == 1 ? "解封账号" : "封禁账号");
        log.setType(status == 1 ? "success" : "danger"); // 封禁显示红色，解封显示绿色
        log.setDetail("将用户 ID: " + id + " 的状态修改为: " + (status == 1 ? "正常" : "禁用"));
        logMapper.insert(log);

        return Result.success(res);
    }

    /**
     * 4. 重置密码为 123456
     */
    @PostMapping("/reset-pwd/{id}")
    public Result<Boolean> resetPwd(@PathVariable Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        // 【修改】调用 encoder 动态生成哈希，不再硬编码
        user.setPassword(passwordEncoder.encode("123456"));
        return Result.success(userService.updateById(user));
        
    }

    /**
     * 5. 用户修改个人资料 (非敏感字段)
     */
    @PostMapping("/update-profile")
    public Result<Boolean> updateProfile(@RequestBody SysUser user) {
        SysUser updateData = new SysUser();
        updateData.setId(user.getId());
        updateData.setRealName(user.getRealName());
        updateData.setPhone(user.getPhone());
        updateData.setAvatar(user.getAvatar());
        
        return Result.success(userService.updateById(updateData));
    }

    /**
     * 6. 【新增】个人中心 - 修改安全密码
     */
    @PostMapping("/update-password")
    public Result<Boolean> updatePassword(@RequestBody Map<String, String> params) {
        Long userId = Long.valueOf(params.get("userId"));
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        SysUser user = userService.getById(userId);
        if (user == null) {
            return Result.error(400, "用户异常");
        }

        // 比对旧密码是否正确（明文与密文比对）
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error(400, "原密码错误，修改失败！");
        }

        // 新密码加密入库
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateById(user);

        return Result.success(true);
    }

}