package com.example.demo.service.impl;

import com.example.demo.dto.IdleClassroomDTO;
import com.example.demo.mapper.BizClassroomScheduleMapper;
import com.example.demo.service.BizClassroomScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class BizClassroomScheduleServiceImpl implements BizClassroomScheduleService {

    @Autowired
    private BizClassroomScheduleMapper scheduleMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<IdleClassroomDTO> getIdleClassrooms(LocalDate date, Integer period) {
        // 1. 构造 Redis 的 Key (例如： smart_campus:idle_room:2026-05-20:3 )
        String redisKey = "smart_campus:idle_room:" + date.toString() + ":" + period;

        // 2. 尝试从 Redis 内存中获取数据
        Object cachedData = redisTemplate.opsForValue().get(redisKey);
        
        if (cachedData != null) {
            // 如果 Redis 里有，直接返回，这就是抗住高并发的秘密！完全不走数据库！
            System.out.println("【Redis 缓存命中】直接从内存返回空闲教室数据...");
            return (List<IdleClassroomDTO>) cachedData;
        }

        // 3. 如果 Redis 里没有（缓存未命中），则去查询 MySQL 数据库
        System.out.println("【Redis 缓存未命中】查询 MySQL 数据库...");
        List<IdleClassroomDTO> idleClassrooms = scheduleMapper.findIdleClassrooms(date, period);

        // 4. 将查到的结果塞入 Redis，并设置一个过期时间（比如 2 小时）
        // 这样接下来 2 小时内的所有重复查询，都会被上面的第 2 步直接拦截并返回
        if (idleClassrooms != null && !idleClassrooms.isEmpty()) {
            redisTemplate.opsForValue().set(redisKey, idleClassrooms, 2, TimeUnit.HOURS);
            System.out.println("【Redis 缓存写入】已将数据库查询结果同步至 Redis！");
        }

        return idleClassrooms;
    }
}