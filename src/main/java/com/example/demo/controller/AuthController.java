package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.dto.LoginDTO;
import com.example.demo.entity.SysUser;
import com.example.demo.service.SysUserService;
import com.example.demo.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private SysUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        // 1. 根据用户名查询用户
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginDTO.getUsername());
        SysUser user = userService.getOne(queryWrapper);

        // 2. 校验用户是否存在以及密码是否正确
        if (user == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return Result.error(400, "用户名或密码错误");
        }

        // 3. 判断账号状态是否被禁用
        if (user.getStatus() == 0) {
            return Result.error(401, "账号已被禁用，请联系管理员");
        }

        // 4. 生成 JWT Token
        String token = jwtUtils.generateToken(user.getUsername(), user.getRoleId());

        // 5. 封装返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("roleId", user.getRoleId());

        return Result.success(data);
    }
}