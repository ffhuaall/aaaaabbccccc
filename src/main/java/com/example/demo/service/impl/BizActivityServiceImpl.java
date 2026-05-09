package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.UserItemScoreDTO;
import com.example.demo.entity.BizActivity;
import com.example.demo.entity.BizActivityRegistration;
import com.example.demo.mapper.BizActivityMapper;
import com.example.demo.mapper.BizActivityRegistrationMapper;
import com.example.demo.service.BizActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BizActivityServiceImpl extends ServiceImpl<BizActivityMapper, BizActivity> implements BizActivityService {

    @Autowired
    private BizActivityMapper activityMapper;

    @Autowired
    private BizActivityRegistrationMapper registrationMapper;
    /**
     * 基于用户的协同过滤推荐算法
     * @param targetUserId 当前需要被推荐的学生ID
     * @param topN 推荐多少个活动
     * @return 推荐的活动列表
     */
    @Override
    public List<BizActivity> recommendActivities(Long targetUserId, int topN) {
        //从报名表捞取所有有效的报名记录作为数据源
        QueryWrapper<BizActivityRegistration> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); 
        List<BizActivityRegistration> allRegistrations = registrationMapper.selectList(wrapper);

        //将报名记录转换为评分矩阵: Map<用户ID, Map<活动ID, 评分>>
        Map<Long, Map<Long, Double>> userItemMatrix = new HashMap<>();
        for (BizActivityRegistration reg : allRegistrations) {
            //只要报名了，我们就认为该用户对这个活动有极强的兴趣，记为 1.0 分
            userItemMatrix.computeIfAbsent(reg.getUserId(), k -> new HashMap<>())
                          .put(reg.getActivityId(), 1.0);
        }

        Map<Long, Double> targetUserItems = userItemMatrix.get(targetUserId);
        
        //冷启动
        if (targetUserItems == null || targetUserItems.isEmpty()) {
            System.out.println("【冷启动拦截】该用户无报名记录，执行基于热度的全局推荐策略");
            //降级策略：直接查出全站最新的/最热门的活动推荐给他
            QueryWrapper<BizActivity> hotWrapper = new QueryWrapper<>();
            hotWrapper.eq("status", 1).orderByDesc("id").last("LIMIT " + topN);
            return this.list(hotWrapper);
        }

        //计算目标用户与其他用户的余弦相似度
        Map<Long, Double> userSimilarities = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Double>> entry : userItemMatrix.entrySet()) {
            Long otherUserId = entry.getKey();
            if (otherUserId.equals(targetUserId)) continue;

            Map<Long, Double> otherUserItems = entry.getValue();
            double similarity = calculateCosineSimilarity(targetUserItems, otherUserItems);
            if (similarity > 0) {
                userSimilarities.put(otherUserId, similarity);
            }
        }

        //根据相似度给候选活动打分
        Map<Long, Double> candidateActivityScores = new HashMap<>();
        for (Map.Entry<Long, Double> simEntry : userSimilarities.entrySet()) {
            Long similarUserId = simEntry.getKey();
            Double similarity = simEntry.getValue();

            Map<Long, Double> similarUserItems = userItemMatrix.get(similarUserId);
            for (Map.Entry<Long, Double> itemEntry : similarUserItems.entrySet()) {
                Long activityId = itemEntry.getKey();
                //核心过滤：只推荐目标用户没报名过的活动
                if (!targetUserItems.containsKey(activityId)) {
                    double currentScore = candidateActivityScores.getOrDefault(activityId, 0.0);
                    candidateActivityScores.put(activityId, currentScore + (similarity * itemEntry.getValue()));
                }
            }
        }

        //对候选活动按得分降序排序，取前 TopN 个
        List<Long> recommendedActivityIds = candidateActivityScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        //兜底返回与查询
        if (recommendedActivityIds.isEmpty()) {
            System.out.println("【算法兜底】没有算出合适的推荐，降级为全局推荐");
            QueryWrapper<BizActivity> hotWrapper = new QueryWrapper<>();
            hotWrapper.eq("status", 1).orderByDesc("id").last("LIMIT " + topN);
            return this.list(hotWrapper);
        }
        return this.listByIds(recommendedActivityIds);
    }

    //计算两个用户评分向量的余弦相似度
    private double calculateCosineSimilarity(Map<Long, Double> user1Items, Map<Long, Double> user2Items) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (Map.Entry<Long, Double> entry : user1Items.entrySet()) {
            Long itemId = entry.getKey();
            Double rating1 = entry.getValue();
            if (user2Items.containsKey(itemId)) {
                dotProduct += rating1 * user2Items.get(itemId);
            }
            norm1 += Math.pow(rating1, 2);
        }

        for (Double rating2 : user2Items.values()) {
            norm2 += Math.pow(rating2, 2);
        }

        if (norm1 == 0 || norm2 == 0) return 0.0;
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}