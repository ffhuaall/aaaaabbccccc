package com.example.demo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    // 密钥 (至少32个字符，实际项目中应放在 yml 配置文件中)
    private static final String SECRET_KEY = "SmartCampusPortalSecretKeyForJwtAuthentication";
    // 过期时间 (这里设置为 24 小时)
    private static final long EXPIRE_TIME = 1000 * 60 * 60 * 24;

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    /**
     * 生成 Token
     */
    public String generateToken(String username, Integer roleId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roleId", roleId) // 自定义载荷，存入角色ID
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME)) // 过期时间
                .signWith(key, SignatureAlgorithm.HS256) // 签名算法
                .compact();
    }

    /**
     * 解析 Token
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}