package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class DemoApplicationTests {

    @Test
    void contextLoads() {
    }

    // 新增这个测试方法
    @Test
    void generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashPassword = encoder.encode("123456");
        System.out.println("=====================================");
        System.out.println("123456 的加密结果是: " + hashPassword);
        System.out.println("=====================================");
    }
}