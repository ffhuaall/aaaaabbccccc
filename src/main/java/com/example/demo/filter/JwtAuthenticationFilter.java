package com.example.demo.filter;

import com.example.demo.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 获取请求头中的 Token (标准规范下，请求头名称为 Authorization)
        String header = request.getHeader("Authorization");
        String token = null;

        // 前端传来的 Token 规范格式通常是 "Bearer eyJhbGci..." (注意 Bearer 后面有个空格)
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        // 2. 如果没有 Token，直接放行（因为有些接口如 /login 本来就不需要 Token，
        // 如果是需要权限的接口，放行后会被 Spring Security 拦截器按规则拦截并报 403/401）
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 尝试解析 Token
        try {
            Claims claims = jwtUtils.parseToken(token);
            String username = claims.getSubject();
            
            // 4. 解析成功，说明是合法用户，将认证信息存入 Spring Security 的上下文
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 第三个参数 authorities 是用户的权限列表，目前角色权限没细化，先给一个空集合
                UsernamePasswordAuthenticationToken authenticationToken = 
                        new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
                
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (Exception e) {
            // Token 过期或被篡改，解析失败
            System.out.println("Token验证失败: " + e.getMessage());
        }

        // 5. 必须调用 doFilter，让请求继续往下走
        filterChain.doFilter(request, response);
    }
}