package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.BizActivity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BizActivityMapper extends BaseMapper<BizActivity> {
    // 继承了 BaseMapper，MyBatis-Plus 自动帮我们实现了基本的增删改查，无需写 XML
}