package com.example.demo.config;

import com.example.demo.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    //注入自定义的JWT过滤器
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
            //.authorizeHttpRequests(auth -> auth
            //      .requestMatchers("/auth/login").permitAll() // 仅放行登录接口
            //     // .requestMatchers("/activity/**").permitAll()
            //     // .requestMatchers("/classroom/**").permitAll()
            //     // .requestMatchers("/course/**").permitAll()
            //     // .requestMatchers("/dashboard/**").permitAll()
            //     // .requestMatchers("/lost-found/**").permitAll()
            //     // .requestMatchers("/repair/**").permitAll()
            //     // .requestMatchers("/message/**").permitAll()
            //     // .requestMatchers("/file/upload").permitAll()
            //     // .requestMatchers("/picture/**").permitAll()
            //     // .requestMatchers("/user**").permitAll()
            //     // .anyRequest().authenticated()
            //);

        //将JWT过滤器添加到UsernamePasswordAuthenticationFilter之前
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}