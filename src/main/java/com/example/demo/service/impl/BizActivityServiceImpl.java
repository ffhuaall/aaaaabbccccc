package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.UserItemScoreDTO;
import com.example.demo.entity.BizActivity;
import com.example.demo.mapper.BizActivityMapper;
import com.example.demo.service.BizActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BizActivityServiceImpl extends ServiceImpl<BizActivityMapper, BizActivity> implements BizActivityService {

    @Autowired
    private BizActivityMapper activityMapper;

    /**
     * 基于用户的协同过滤推荐算法
     * @param targetUserId 当前需要被推荐的学生ID
     * @param topN 推荐多少个活动
     * @return 推荐的活动列表
     */
    public List<BizActivity> recommendActivities(Long targetUserId, int topN) {
        // 1. 获取所有用户的评分数据
        List<UserItemScoreDTO> allScores = activityMapper.getUserItemScores();
        if (allScores == null || allScores.isEmpty()) {
            return Collections.emptyList(); // 没数据就不推荐
        }

        // 2. 将数据转换为: Map<用户ID, Map<活动ID, 评分>>
        Map<Long, Map<Long, Double>> userItemMatrix = new HashMap<>();
        for (UserItemScoreDTO score : allScores) {
            userItemMatrix.computeIfAbsent(score.getUserId(), k -> new HashMap<>())
                          .put(score.getActivityId(), score.getScore());
        }

        Map<Long, Double> targetUserItems = userItemMatrix.get(targetUserId);
        if (targetUserItems == null || targetUserItems.isEmpty()) {
            // 如果这个新用户没有任何行为（冷启动问题），可以直接返回最新发布的活动（这里为了简便暂不处理）
            return Collections.emptyList();
        }

        // 3. 计算目标用户与其他用户的余弦相似度
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

        // 4. 根据相似度给候选活动打分
        Map<Long, Double> candidateActivityScores = new HashMap<>();
        for (Map.Entry<Long, Double> simEntry : userSimilarities.entrySet()) {
            Long similarUserId = simEntry.getKey();
            Double similarity = simEntry.getValue();

            Map<Long, Double> similarUserItems = userItemMatrix.get(similarUserId);
            for (Map.Entry<Long, Double> itemEntry : similarUserItems.entrySet()) {
                Long activityId = itemEntry.getKey();
                // 核心过滤：只推荐目标用户没看过的活动
                if (!targetUserItems.containsKey(activityId)) {
                    double currentScore = candidateActivityScores.getOrDefault(activityId, 0.0);
                    // 候选活动的得分 = 累加 (相似用户的相似度 * 相似用户对该活动的评分)
                    candidateActivityScores.put(activityId, currentScore + (similarity * itemEntry.getValue()));
                }
            }
        }

        // 5. 对候选活动按得分降序排序，取前 TopN 个
        List<Long> recommendedActivityIds = candidateActivityScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 6. 根据 ID 查询真实的活动对象返回
        if (recommendedActivityIds.isEmpty()) {
            return Collections.emptyList();
        }
        return this.listByIds(recommendedActivityIds);
    }

    /**
     * 计算两个用户评分向量的余弦相似度
     */
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