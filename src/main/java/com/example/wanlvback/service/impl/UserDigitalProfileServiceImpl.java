package com.example.wanlvback.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.example.wanlvback.constant.MessageConstant;
import com.example.wanlvback.exception.BaseException;
import com.example.wanlvback.mapper.SysNormalUserMapper;
import com.example.wanlvback.mapper.UserDigitalProfileMapper;
import com.example.wanlvback.mapper.VisitorSessionMapper;
import com.example.wanlvback.pojo.entity.SysNormalUser;
import com.example.wanlvback.pojo.entity.UserDigitalProfile;
import com.example.wanlvback.pojo.entity.VisitorSession;
import com.example.wanlvback.pojo.vo.UserDigitalProfileVO;
import com.example.wanlvback.result.PageResult;
import com.example.wanlvback.service.UserDigitalProfileService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 用户数字画像业务实现类。
 */
@Service
@Slf4j
public class UserDigitalProfileServiceImpl implements UserDigitalProfileService {

    @Autowired
    private UserDigitalProfileMapper userDigitalProfileMapper;

    @Autowired
    private VisitorSessionMapper visitorSessionMapper;

    @Autowired
    private SysNormalUserMapper sysNormalUserMapper;

    /**
     * 查询全部用户画像，返回给超级管理员后台使用。
     */
    @Override
    public PageResult pageAll(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(normalizePageNum(pageNum), normalizePageSize(pageSize));
        Page<UserDigitalProfile> page = userDigitalProfileMapper.listAll();
        return new PageResult(page.getTotal(), page.getResult().stream()
                .map(this::buildVO)
                .toList());
    }

    /**
     * 查询用户数字画像。
     */
    @Override
    public UserDigitalProfileVO getByUserId(Long userId) {
        if (userId == null) {
            throw new BaseException(MessageConstant.USER_ID_REQUIRED);
        }
        UserDigitalProfile profile = userDigitalProfileMapper.getByUserId(userId);
        if (profile == null) {
            throw new BaseException(MessageConstant.USER_PROFILE_NOT_FOUND);
        }
        return buildVO(profile);
    }

    /**
     * 基于已生成的日报刷新用户数字画像。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDigitalProfileVO refreshByUserId(Long userId) {
        if (userId == null) {
            throw new BaseException(MessageConstant.USER_ID_REQUIRED);
        }

        SysNormalUser normalUser = sysNormalUserMapper.getById(userId);
        if (normalUser == null || isDeleted(normalUser.getDeleted())) {
            throw new BaseException(MessageConstant.USER_NOT_FOUND);
        }

        List<VisitorSession> sessions = visitorSessionMapper.listAnalyzedByUserId(userId);
        if (sessions == null || sessions.isEmpty()) {
            throw new BaseException(MessageConstant.USER_PROFILE_SOURCE_EMPTY);
        }

        UserDigitalProfile profile = buildProfile(normalUser, sessions);
        userDigitalProfileMapper.upsert(profile);
        UserDigitalProfile latestProfile = userDigitalProfileMapper.getByUserId(profile.getUserId());
        return buildVO(latestProfile == null ? profile : latestProfile);
    }

    private UserDigitalProfile buildProfile(SysNormalUser normalUser, List<VisitorSession> sessions) {
        Map<String, Integer> interestTagCounter = new LinkedHashMap<>();
        Map<String, Integer> focusTopicCounter = new LinkedHashMap<>();
        Map<String, Integer> serviceNeedCounter = new LinkedHashMap<>();
        Map<String, Integer> knowledgeGapCounter = new LinkedHashMap<>();
        Map<String, Integer> sentimentCounter = new LinkedHashMap<>();

        addAll(interestTagCounter, parseJsonArrayField(normalUser.getInterestTags()));

        BigDecimal sentimentScoreSum = BigDecimal.ZERO;
        int sentimentScoreCount = 0;
        int totalInteractionCount = 0;
        LocalDate lastAnalyzedDate = null;

        for (VisitorSession session : sessions) {
            addAll(interestTagCounter, parseJsonArrayField(session.getInterestTags()));
            addAll(focusTopicCounter, parseJsonArrayField(session.getFocusTopics()));
            addAll(serviceNeedCounter, parseJsonArrayField(session.getServiceSuggestions()));
            addAll(knowledgeGapCounter, parseJsonArrayField(session.getKnowledgeGapPoints()));

            if (StringUtils.hasText(session.getOverallSentiment())) {
                increment(sentimentCounter, session.getOverallSentiment().trim());
            }
            if (session.getSentimentScore() != null) {
                sentimentScoreSum = sentimentScoreSum.add(session.getSentimentScore());
                sentimentScoreCount++;
            }
            if (session.getInteractionCount() != null) {
                totalInteractionCount += session.getInteractionCount();
            }
            if (session.getReportDate() != null
                    && (lastAnalyzedDate == null || session.getReportDate().isAfter(lastAnalyzedDate))) {
                lastAnalyzedDate = session.getReportDate();
            }
        }

        List<String> interestTags = topKeys(interestTagCounter, 10);
        List<String> focusTopics = topKeys(focusTopicCounter, 10);
        List<String> serviceNeeds = topKeys(serviceNeedCounter, 8);
        List<String> knowledgeGaps = topKeys(knowledgeGapCounter, 8);
        BigDecimal sentimentScoreAvg = average(sentimentScoreSum, sentimentScoreCount);
        String sentimentTendency = resolveSentimentTendency(sentimentScoreAvg, sentimentCounter);
        String activityLevel = resolveActivityLevel(sessions.size(), totalInteractionCount);
        String travelStyle = resolveTravelStyle(interestTags, focusTopics);
        String profileName = travelStyle + "用户";
        int profileScore = resolveProfileScore(sessions.size(), interestTags, focusTopics, serviceNeeds, knowledgeGaps, sentimentScoreAvg);

        JSONObject profileJson = new JSONObject(true);
        profileJson.put("interestTags", interestTags);
        profileJson.put("focusTopics", focusTopics);
        profileJson.put("serviceNeeds", serviceNeeds);
        profileJson.put("knowledgeGaps", knowledgeGaps);
        profileJson.put("travelStyle", travelStyle);
        profileJson.put("activityLevel", activityLevel);
        profileJson.put("sentimentTendency", sentimentTendency);
        profileJson.put("sentimentScoreAvg", sentimentScoreAvg);
        profileJson.put("profileScore", profileScore);
        profileJson.put("sourceSessionCount", sessions.size());
        profileJson.put("lastAnalyzedDate", lastAnalyzedDate);

        return UserDigitalProfile.builder()
                .userId(normalUser.getId())
                .profileName(profileName)
                .interestTags(JSON.toJSONString(interestTags))
                .focusTopics(JSON.toJSONString(focusTopics))
                .serviceNeeds(JSON.toJSONString(serviceNeeds))
                .knowledgeGaps(JSON.toJSONString(knowledgeGaps))
                .travelStyle(travelStyle)
                .activityLevel(activityLevel)
                .sentimentTendency(sentimentTendency)
                .sentimentScoreAvg(sentimentScoreAvg)
                .profileScore(profileScore)
                .sourceSessionCount(sessions.size())
                .lastAnalyzedDate(lastAnalyzedDate)
                .profileJson(profileJson.toJSONString())
                .build();
    }

    private UserDigitalProfileVO buildVO(UserDigitalProfile profile) {
        return UserDigitalProfileVO.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .profileName(profile.getProfileName())
                .interestTags(parseJsonArrayField(profile.getInterestTags()))
                .focusTopics(parseJsonArrayField(profile.getFocusTopics()))
                .serviceNeeds(parseJsonArrayField(profile.getServiceNeeds()))
                .knowledgeGaps(parseJsonArrayField(profile.getKnowledgeGaps()))
                .travelStyle(profile.getTravelStyle())
                .activityLevel(profile.getActivityLevel())
                .sentimentTendency(profile.getSentimentTendency())
                .sentimentScoreAvg(profile.getSentimentScoreAvg())
                .profileScore(profile.getProfileScore())
                .sourceSessionCount(profile.getSourceSessionCount())
                .lastAnalyzedDate(profile.getLastAnalyzedDate())
                .profileJson(profile.getProfileJson())
                .createTime(profile.getCreateTime())
                .updateTime(profile.getUpdateTime())
                .build();
    }

    private void addAll(Map<String, Integer> counter, List<String> values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                increment(counter, value.trim());
            }
        }
    }

    private void increment(Map<String, Integer> counter, String key) {
        counter.put(key, counter.getOrDefault(key, 0) + 1);
    }

    private List<String> topKeys(Map<String, Integer> counter, int limit) {
        return counter.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .filter(Objects::nonNull)
                .toList();
    }

    private BigDecimal average(BigDecimal sum, int count) {
        if (count <= 0) {
            return null;
        }
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private String resolveSentimentTendency(BigDecimal sentimentScoreAvg, Map<String, Integer> sentimentCounter) {
        if (sentimentScoreAvg != null) {
            if (sentimentScoreAvg.compareTo(BigDecimal.valueOf(70)) >= 0) {
                return "positive";
            }
            if (sentimentScoreAvg.compareTo(BigDecimal.valueOf(40)) <= 0) {
                return "negative";
            }
            return "neutral";
        }
        return topKeys(sentimentCounter, 1).stream().findFirst().orElse("unknown");
    }

    private String resolveActivityLevel(int sessionCount, int totalInteractionCount) {
        BigDecimal averageInteraction = sessionCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(totalInteractionCount).divide(BigDecimal.valueOf(sessionCount), 2, RoundingMode.HALF_UP);
        if (sessionCount >= 10 || averageInteraction.compareTo(BigDecimal.valueOf(8)) >= 0) {
            return "高活跃";
        }
        if (sessionCount >= 3 || averageInteraction.compareTo(BigDecimal.valueOf(3)) >= 0) {
            return "中活跃";
        }
        return "低活跃";
    }

    private String resolveTravelStyle(List<String> interestTags, List<String> focusTopics) {
        String text = String.join(",", interestTags) + "," + String.join(",", focusTopics);
        if (containsAny(text, "亲子", "儿童", "孩子", "家庭")) {
            return "亲子游";
        }
        if (containsAny(text, "历史", "文化", "建筑", "讲解")) {
            return "文化深度游";
        }
        if (containsAny(text, "门票", "价格", "费用", "优惠", "预算")) {
            return "预算关注型";
        }
        if (containsAny(text, "路线", "规划", "行程", "游线")) {
            return "路线规划型";
        }
        if (containsAny(text, "服务", "咨询", "天气", "设施")) {
            return "服务咨询型";
        }
        return "综合咨询型";
    }

    private boolean containsAny(String text, String... keywords) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private int resolveProfileScore(int sessionCount, List<String> interestTags, List<String> focusTopics,
                                    List<String> serviceNeeds, List<String> knowledgeGaps, BigDecimal sentimentScoreAvg) {
        int score = Math.min(sessionCount * 10, 30);
        score += Math.min(interestTags.size() * 5, 25);
        score += Math.min(focusTopics.size() * 4, 20);
        score += Math.min(serviceNeeds.size() * 3, 12);
        score += Math.min(knowledgeGaps.size() * 2, 8);
        if (sentimentScoreAvg != null) {
            score += 5;
        }
        return Math.min(score, 100);
    }

    private List<String> parseJsonArrayField(String jsonArrayText) {
        if (!StringUtils.hasText(jsonArrayText)) {
            return new ArrayList<>();
        }
        try {
            List<String> values = JSON.parseObject(jsonArrayText, new TypeReference<List<String>>() {
            });
            return values == null ? new ArrayList<>() : values;
        } catch (RuntimeException ex) {
            log.warn("解析用户画像数组字段失败，json={}", jsonArrayText, ex);
            return new ArrayList<>();
        }
    }

    private boolean isDeleted(Integer deleted) {
        return deleted != null && deleted == 1;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }
}
