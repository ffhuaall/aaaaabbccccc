package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.dto.UserItemScoreDTO;
import com.example.demo.entity.BizActivity;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BizActivityMapper extends BaseMapper<BizActivity> {
    @Select("SELECT user_id, activity_id, SUM(score) as score FROM (" +
            "  SELECT user_id, activity_id, 2 as score FROM biz_activity_registration " +
            "  UNION ALL " +
            "  SELECT user_id, activity_id, 1 as score FROM biz_activity_collection" +
            ") t GROUP BY user_id, activity_id")
    List<UserItemScoreDTO> getUserItemScores();

    /**
     * 获取参与人数排名前 5 的热门活动 (供数据大屏使用)
     * 返回 Map 包含 title (活动标题) 和 reg_count (报名人数)
     */
    @Select("SELECT a.title, COUNT(r.id) as reg_count " +
            "FROM biz_activity a " +
            "LEFT JOIN biz_activity_registration r ON a.id = r.activity_id " +
            "GROUP BY a.id, a.title " +
            "ORDER BY reg_count DESC " +
            "LIMIT 5")
    List<Map<String, Object>> getTopPopularActivities();
}