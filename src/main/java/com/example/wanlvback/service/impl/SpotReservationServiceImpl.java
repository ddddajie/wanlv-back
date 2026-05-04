package com.example.wanlvback.service.impl;

import com.example.wanlvback.exception.BaseException;
import com.example.wanlvback.mapper.ScenicAreaMapper;
import com.example.wanlvback.mapper.ScenicSpotMapper;
import com.example.wanlvback.mapper.SpotReservationOrderMapper;
import com.example.wanlvback.mapper.SpotReservationRuleMapper;
import com.example.wanlvback.mapper.SpotReservationSlotMapper;
import com.example.wanlvback.mapper.SysNormalUserMapper;
import com.example.wanlvback.pojo.dto.AgentReservationOrderDTO;
import com.example.wanlvback.pojo.dto.SpotReservationCancelDTO;
import com.example.wanlvback.pojo.dto.SpotReservationCreateDTO;
import com.example.wanlvback.pojo.dto.SpotReservationRuleCreateDTO;
import com.example.wanlvback.pojo.dto.SpotReservationRuleStatusDTO;
import com.example.wanlvback.pojo.dto.SpotReservationRuleUpdateDTO;
import com.example.wanlvback.pojo.dto.SpotReservationSlotCreateDTO;
import com.example.wanlvback.pojo.dto.SpotReservationSlotGenerateDTO;
import com.example.wanlvback.pojo.dto.SpotReservationSlotUpdateDTO;
import com.example.wanlvback.pojo.entity.ScenicArea;
import com.example.wanlvback.pojo.entity.ScenicSpot;
import com.example.wanlvback.pojo.entity.SpotReservationOrder;
import com.example.wanlvback.pojo.entity.SpotReservationRule;
import com.example.wanlvback.pojo.entity.SpotReservationSlot;
import com.example.wanlvback.pojo.entity.SysNormalUser;
import com.example.wanlvback.pojo.vo.AgentReservationCancelResultVO;
import com.example.wanlvback.pojo.vo.AgentReservationOrderResultVO;
import com.example.wanlvback.pojo.vo.AgentReservationSlotMatchVO;
import com.example.wanlvback.pojo.vo.AgentReservationSlotRecommendVO;
import com.example.wanlvback.pojo.vo.AgentReservationSpotVO;
import com.example.wanlvback.pojo.vo.ReservationDashboardVO;
import com.example.wanlvback.pojo.vo.ReservationEnabledSpotVO;
import com.example.wanlvback.pojo.vo.SpotReservationGenerateVO;
import com.example.wanlvback.pojo.vo.SpotReservationOrderVO;
import com.example.wanlvback.pojo.vo.SpotReservationRuleVO;
import com.example.wanlvback.pojo.vo.SpotReservationSlotVO;
import com.example.wanlvback.pojo.vo.SpotReservationSlotsQueryVO;
import com.example.wanlvback.result.PageResult;
import com.example.wanlvback.service.SpotReservationService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SpotReservationServiceImpl implements SpotReservationService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_EXPIRED = "EXPIRED";
    private static final String STATUS_FRONTEND = "FRONTEND";
    private static final String SOURCE_AGENT = "AGENT";
    private static final String MATCH_TYPE_EXACT = "EXACT";
    private static final String MATCH_TYPE_NEAREST = "NEAREST";
    private static final String MATCH_TYPE_FIRST_AVAILABLE = "FIRST_AVAILABLE";
    private static final String MATCH_TYPE_NO_AVAILABLE = "NO_AVAILABLE";
    private static final DateTimeFormatter RESERVATION_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter AGENT_REPLY_DATE = DateTimeFormatter.ofPattern("yyyy年M月d日");
    private static final DateTimeFormatter DASHBOARD_TREND_LABEL = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter DASHBOARD_ACTIVITY_TIME = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    private static final Set<String> SOURCE_TYPES = Set.of("FRONTEND", "AGENT", "ADMIN");

    @Autowired
    private SpotReservationRuleMapper ruleMapper;

    @Autowired
    private SpotReservationSlotMapper slotMapper;

    @Autowired
    private SpotReservationOrderMapper orderMapper;

    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    @Autowired
    private ScenicAreaMapper scenicAreaMapper;

    @Autowired
    private SysNormalUserMapper sysNormalUserMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpotReservationRuleVO createRule(SpotReservationRuleCreateDTO dto) {
        validateRuleCreate(dto);
        ScenicSpot spot = requireSpot(dto.getSpotId());
        validateSpotArea(dto.getScenicAreaId(), spot);

        LocalDateTime now = LocalDateTime.now();
        SpotReservationRule rule = new SpotReservationRule();
        BeanUtils.copyProperties(dto, rule);
        rule.setAdvanceDays(defaultNumber(dto.getAdvanceDays(), 7));
        rule.setStatus(1);
        rule.setCreateTime(now);
        rule.setUpdateTime(now);
        ruleMapper.insert(rule);
        return buildRuleVO(ruleMapper.getById(rule.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpotReservationRuleVO updateRule(Long id, SpotReservationRuleUpdateDTO dto) {
        if (id == null || dto == null) {
            throw new BaseException("预约规则参数不能为空");
        }
        SpotReservationRule existRule = requireRule(id);
        LocalTime startTime = dto.getStartTime() == null ? existRule.getStartTime() : dto.getStartTime();
        LocalTime endTime = dto.getEndTime() == null ? existRule.getEndTime() : dto.getEndTime();
        validateTimeRange(startTime, endTime);
        validatePositive(dto.getTotalCapacity(), "预约容量必须大于0");
        validateWeekDays(dto.getWeekDays());
        validatePositive(dto.getAdvanceDays(), "提前生成天数必须大于0");
        validateStatus(dto.getStatus());

        SpotReservationRule rule = new SpotReservationRule();
        BeanUtils.copyProperties(dto, rule);
        rule.setId(id);
        ruleMapper.updateById(rule);
        return buildRuleVO(ruleMapper.getById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateRuleStatus(Long id, SpotReservationRuleStatusDTO dto) {
        if (id == null || dto == null || dto.getStatus() == null) {
            throw new BaseException("预约规则状态不能为空");
        }
        requireRule(id);
        validateStatus(dto.getStatus());
        return ruleMapper.updateStatus(id, dto.getStatus(), dto.getUpdateBy()) > 0;
    }

    @Override
    public PageResult pageRules(Integer pageNum, Integer pageSize, Long scenicAreaId, Long spotId, Integer status) {
        PageHelper.startPage(normalizePageNum(pageNum), normalizePageSize(pageSize));
        Page<SpotReservationRule> page = ruleMapper.pageQuery(scenicAreaId, spotId, status);
        return new PageResult(page.getTotal(), page.getResult().stream().map(this::buildRuleVO).collect(Collectors.toList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpotReservationGenerateVO generateSlots(SpotReservationSlotGenerateDTO dto) {
        SpotReservationSlotGenerateDTO request = dto == null ? new SpotReservationSlotGenerateDTO() : dto;
        int generatedCount = 0;
        int skipCount = 0;
        List<SpotReservationRule> rules = ruleMapper.listEnabledForGenerate(request.getScenicAreaId(), request.getSpotId());
        LocalDate today = LocalDate.now();
        for (SpotReservationRule rule : rules) {
            int days = defaultNumber(request.getDays(), defaultNumber(rule.getAdvanceDays(), 7));
            if (days < 1) {
                throw new BaseException("生成天数必须大于0");
            }
            for (int i = 0; i < days; i++) {
                LocalDate visitDate = today.plusDays(i);
                if (!matchesWeekDays(rule.getWeekDays(), visitDate)) {
                    skipCount++;
                    continue;
                }
                if (slotMapper.getBySpotDateTime(rule.getSpotId(), visitDate, rule.getStartTime(), rule.getEndTime()) != null) {
                    skipCount++;
                    continue;
                }
                SpotReservationSlot slot = SpotReservationSlot.builder().scenicAreaId(rule.getScenicAreaId()).spotId(rule.getSpotId()).ruleId(rule.getId()).visitDate(visitDate).startTime(rule.getStartTime()).endTime(rule.getEndTime()).totalCapacity(rule.getTotalCapacity()).reservedCount(0).status(1).createBy(rule.getCreateBy()).createTime(LocalDateTime.now()).updateTime(LocalDateTime.now()).build();
                try {
                    slotMapper.insert(slot);
                    generatedCount++;
                } catch (DuplicateKeyException ex) {
                    log.info("skip duplicate reservation slot, spotId={}, visitDate={}, startTime={}, endTime={}", rule.getSpotId(), visitDate, rule.getStartTime(), rule.getEndTime());
                    skipCount++;
                }
            }
        }
        return SpotReservationGenerateVO.builder().generatedCount(generatedCount).skipCount(skipCount).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpotReservationSlotVO createSlot(SpotReservationSlotCreateDTO dto) {
        validateSlotCreate(dto);
        ScenicSpot spot = requireSpot(dto.getSpotId());
        validateSpotArea(dto.getScenicAreaId(), spot);
        if (slotMapper.getBySpotDateTime(dto.getSpotId(), dto.getVisitDate(), dto.getStartTime(), dto.getEndTime()) != null) {
            throw new BaseException("预约时段已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        SpotReservationSlot slot = new SpotReservationSlot();
        BeanUtils.copyProperties(dto, slot);
        slot.setReservedCount(0);
        slot.setStatus(1);
        slot.setCreateTime(now);
        slot.setUpdateTime(now);
        slotMapper.insert(slot);
        return buildSlotVO(slotMapper.getById(slot.getId()), false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpotReservationSlotVO updateSlot(Long id, SpotReservationSlotUpdateDTO dto) {
        if (id == null || dto == null) {
            throw new BaseException("预约时段参数不能为空");
        }
        SpotReservationSlot existSlot = requireSlot(id);
        validatePositive(dto.getTotalCapacity(), "预约容量必须大于0");
        validateStatus(dto.getStatus());
        if (dto.getTotalCapacity() != null && dto.getTotalCapacity() < defaultNumber(existSlot.getReservedCount(), 0)) {
            throw new BaseException("预约容量不能小于已预约人数");
        }
        SpotReservationSlot slot = new SpotReservationSlot();
        BeanUtils.copyProperties(dto, slot);
        slot.setId(id);
        slotMapper.updateById(slot);
        return buildSlotVO(slotMapper.getById(id), false);
    }

    @Override
    public PageResult pageSlots(Integer pageNum, Integer pageSize, Long scenicAreaId, Long spotId, LocalDate visitDate, LocalDate startDate, LocalDate endDate, Integer status) {
        PageHelper.startPage(normalizePageNum(pageNum), normalizePageSize(pageSize));
        Page<SpotReservationSlot> page = slotMapper.pageQuery(scenicAreaId, spotId, visitDate, startDate, endDate, status);
        return new PageResult(page.getTotal(), page.getResult().stream().map(slot -> buildSlotVO(slot, false)).collect(Collectors.toList()));
    }

    @Override
    public PageResult pageOrders(Integer pageNum, Integer pageSize, Long scenicAreaId, Long spotId, Long userId, LocalDate visitDate, String status, String sourceType, String reservationNo) {
        PageHelper.startPage(normalizePageNum(pageNum), normalizePageSize(pageSize));
        Page<SpotReservationOrder> page = orderMapper.pageQuery(scenicAreaId, spotId, userId, visitDate, normalizeBlank(status), normalizeBlank(sourceType), normalizeBlank(reservationNo));
        return new PageResult(page.getTotal(), page.getResult().stream().map(this::buildOrderVO).collect(Collectors.toList()));
    }

    @Override
    public ReservationDashboardVO getAdminDashboard(Long scenicAreaId, LocalDate date) {
        LocalDate statisticDate = date == null ? LocalDate.now() : date;
        LocalDateTime startTime = statisticDate.atStartOfDay();
        LocalDateTime endTime = statisticDate.plusDays(1).atStartOfDay();

        ReservationDashboardVO.TrendVO todaySummary = orderMapper.getDashboardOrderSummary(scenicAreaId, startTime, endTime);
        ReservationDashboardVO.TrendVO yesterdaySummary = orderMapper.getDashboardOrderSummary(scenicAreaId,
                statisticDate.minusDays(1).atStartOfDay(), statisticDate.atStartOfDay());
        List<ReservationDashboardVO.CapacitySpotVO> capacitySpots = slotMapper.listDashboardCapacitySpots(scenicAreaId, statisticDate);
        List<ReservationDashboardVO.SourceDistributionVO> sourceDistribution = buildSourceDistribution(
                orderMapper.listDashboardSourceDistribution(scenicAreaId, startTime, endTime), getOrderCount(todaySummary));
        List<ReservationDashboardVO.StatusDistributionVO> statusDistribution = buildStatusDistribution(
                orderMapper.listDashboardStatusDistribution(scenicAreaId, startTime, endTime), getOrderCount(todaySummary));
        List<ReservationDashboardVO.PeakTimeVO> peakTimes = buildPeakTimes(
                slotMapper.listDashboardPeakTimes(scenicAreaId, statisticDate, 4));
        List<SpotReservationOrderVO> liveOrders = orderMapper.listDashboardLiveOrders(scenicAreaId, startTime, endTime, 10)
                .stream().map(this::buildOrderVO).collect(Collectors.toList());

        return ReservationDashboardVO.builder()
                .scenicAreas(buildScenicAreaOptions())
                .summary(buildDashboardSummary(todaySummary, yesterdaySummary, capacitySpots, statusDistribution))
                .capacityRanks(capacitySpots.stream().limit(6).collect(Collectors.toList()))
                .sourceDistribution(sourceDistribution)
                .heatSpots(buildHeatSpots(capacitySpots))
                .trend(buildDashboardTrend(scenicAreaId, statisticDate))
                .statusDistribution(statusDistribution)
                .peakTimes(peakTimes)
                .hotSpotRanks(buildHotSpotRanks(capacitySpots))
                .warnings(buildDashboardWarnings(capacitySpots, statusDistribution))
                .liveActivities(buildLiveActivities(liveOrders))
                .build();
    }

    @Override
    public List<ReservationEnabledSpotVO> listReservationEnabledSpots(Long scenicAreaId, String keyword) {
        return scenicSpotMapper.listReservationEnabled(scenicAreaId, keyword).stream().map(spot -> ReservationEnabledSpotVO.builder().spotId(spot.getId()).scenicAreaId(spot.getScenicAreaId()).spotName(spot.getSpotName()).shortIntro(spot.getShortIntro()).reservationEnabled(spot.getReservationEnabled()).reservationNotice(spot.getReservationNotice()).advanceReservationDays(spot.getAdvanceReservationDays()).minAdvanceMinutes(spot.getMinAdvanceMinutes()).build()).collect(Collectors.toList());
    }

    @Override
    public SpotReservationSlotsQueryVO listAvailableSlots(Long spotId, LocalDate visitDate) {
        if (spotId == null || visitDate == null) {
            throw new BaseException("景点ID和预约日期不能为空");
        }
        ScenicSpot spot = requireSpot(spotId);
        validateSpotReservationEnabled(spot);
        validateAdvanceDate(spot, visitDate);
        List<SpotReservationSlotVO> slots = slotMapper.listBySpotAndDate(spotId, visitDate).stream().map(slot -> buildSlotVO(slot, true)).collect(Collectors.toList());
        return SpotReservationSlotsQueryVO.builder().spotId(spot.getId()).spotName(spot.getSpotName()).visitDate(visitDate).slots(slots).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpotReservationOrderVO createOrder(SpotReservationCreateDTO dto) {
        validateOrderCreate(dto);
        if (StringUtils.hasText(dto.getClientRequestId())) {
            SpotReservationOrder existOrder = orderMapper.getByClientRequestId(dto.getClientRequestId());
            if (existOrder != null) {
                return buildOrderVO(existOrder);
            }
        }

        SysNormalUser user = requireActiveUser(dto.getUserId());
        SpotReservationSlot slot = requireSlot(dto.getSlotId());
        ScenicSpot spot = requireSpot(slot.getSpotId());
        validateSpotReservationEnabled(spot);
        validateAdvanceDate(spot, slot.getVisitDate());
        validateMinAdvanceTime(spot, slot);

        int affected = slotMapper.increaseReservedCount(slot.getId(), dto.getVisitorCount());
        if (affected != 1) {
            throw new BaseException("剩余名额不足或当前时段不可预约");
        }

        LocalDateTime now = LocalDateTime.now();
        SpotReservationOrder order = SpotReservationOrder.builder().reservationNo(generateReservationNo()).userId(user.getId()).scenicAreaId(slot.getScenicAreaId()).spotId(slot.getSpotId()).slotId(slot.getId()).visitDate(slot.getVisitDate()).startTime(slot.getStartTime()).endTime(slot.getEndTime()).visitorCount(dto.getVisitorCount()).contactName(dto.getContactName()).contactPhone(dto.getContactPhone()).status(STATUS_CONFIRMED).sourceType(normalizeSourceType(dto.getSourceType())).agentSessionCode(dto.getAgentSessionCode()).clientRequestId(normalizeBlank(dto.getClientRequestId())).remark(dto.getRemark()).createTime(now).updateTime(now).build();
        orderMapper.insert(order);
        return buildOrderVO(orderMapper.getById(order.getId()));
    }

    @Override
    public PageResult pageMyOrders(Integer pageNum, Integer pageSize, Long userId, String status) {
        if (userId == null) {
            throw new BaseException("用户ID不能为空");
        }
        PageHelper.startPage(normalizePageNum(pageNum), normalizePageSize(pageSize));
        Page<SpotReservationOrder> page = orderMapper.pageQuery(null, null, userId, null, normalizeBlank(status), null, null);
        return new PageResult(page.getTotal(), page.getResult().stream().map(this::buildOrderVO).collect(Collectors.toList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelOrder(String reservationNo, SpotReservationCancelDTO dto) {
        if (!StringUtils.hasText(reservationNo) || dto == null || dto.getUserId() == null) {
            throw new BaseException("取消预约参数不能为空");
        }
        SpotReservationOrder order = orderMapper.getByReservationNo(reservationNo);
        if (order == null || !dto.getUserId().equals(order.getUserId())) {
            throw new BaseException("预约订单不存在");
        }
        if (!STATUS_PENDING.equals(order.getStatus()) && !STATUS_CONFIRMED.equals(order.getStatus())) {
            throw new BaseException("当前状态不允许取消");
        }
        int updated = orderMapper.cancelByReservationNo(reservationNo, dto.getUserId(), dto.getCancelReason());
        if (updated != 1) {
            throw new BaseException("当前状态不允许取消");
        }
        int rollback = slotMapper.decreaseReservedCount(order.getSlotId(), order.getVisitorCount());
        if (rollback != 1) {
            throw new BaseException("预约名额回滚失败");
        }
        return true;
    }

    @Override
    public List<AgentReservationSpotVO> searchAgentReservationSpots(Long scenicAreaId, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new BaseException("景点名称关键词不能为空");
        }
        return scenicSpotMapper.listReservationEnabled(scenicAreaId, keyword.trim()).stream().map(spot -> {
            ScenicArea area = scenicAreaMapper.getById(spot.getScenicAreaId());
            return AgentReservationSpotVO.builder()
                    .spotId(spot.getId())
                    .spotName(spot.getSpotName())
                    .scenicAreaId(spot.getScenicAreaId())
                    .scenicName(area == null ? null : area.getScenicName())
                    .reservationEnabled(spot.getReservationEnabled())
                    .reservationNotice(spot.getReservationNotice())
                    .advanceReservationDays(spot.getAdvanceReservationDays())
                    .minAdvanceMinutes(spot.getMinAdvanceMinutes())
                    .matchType(resolveSpotMatchType(spot.getSpotName(), keyword))
                    .confidence(calculateSpotConfidence(spot.getSpotName(), keyword))
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public AgentReservationSlotMatchVO matchAgentReservationSlot(Long spotId, LocalDate visitDate, LocalTime targetTime, Integer visitorCount) {
        int requiredCount = visitorCount == null ? 1 : visitorCount;
        if (requiredCount <= 0) {
            throw new BaseException("预约人数必须大于0");
        }

        SpotReservationSlotsQueryVO queryVO = listAvailableSlots(spotId, visitDate);
        List<SpotReservationSlotVO> candidateSlots = queryVO.getSlots().stream()
                .filter(slot -> Boolean.TRUE.equals(slot.getAvailable()))
                .filter(slot -> defaultNumber(slot.getRemainingCount(), 0) >= requiredCount)
                .collect(Collectors.toList());

        if (candidateSlots.isEmpty()) {
            String replyText = String.format(Locale.CHINA, "%s在%s暂无满足%d人的可预约时段。",
                    queryVO.getSpotName(), visitDate.format(AGENT_REPLY_DATE), requiredCount);
            return AgentReservationSlotMatchVO.builder()
                    .matchedSlot(null)
                    .candidateSlots(candidateSlots)
                    .matchType(MATCH_TYPE_NO_AVAILABLE)
                    .replyText(replyText)
                    .build();
        }

        if (targetTime == null) {
            SpotReservationSlotVO matchedSlot = candidateSlots.get(0);
            return AgentReservationSlotMatchVO.builder()
                    .matchedSlot(matchedSlot)
                    .candidateSlots(candidateSlots)
                    .matchType(MATCH_TYPE_FIRST_AVAILABLE)
                    .replyText(buildSlotMatchReply(queryVO.getSpotName(), matchedSlot, requiredCount, false, null))
                    .build();
        }

        SpotReservationSlotVO exactSlot = candidateSlots.stream()
                .filter(slot -> !targetTime.isBefore(slot.getStartTime()) && targetTime.isBefore(slot.getEndTime()))
                .findFirst()
                .orElse(null);
        if (exactSlot != null) {
            return AgentReservationSlotMatchVO.builder()
                    .matchedSlot(exactSlot)
                    .candidateSlots(candidateSlots)
                    .matchType(MATCH_TYPE_EXACT)
                    .replyText(buildSlotMatchReply(queryVO.getSpotName(), exactSlot, requiredCount, true, targetTime))
                    .build();
        }

        SpotReservationSlotVO nearestSlot = candidateSlots.stream()
                .min(Comparator.comparingLong(slot -> calculateSlotDistanceSeconds(slot, targetTime)))
                .orElse(candidateSlots.get(0));
        return AgentReservationSlotMatchVO.builder()
                .matchedSlot(nearestSlot)
                .candidateSlots(candidateSlots)
                .matchType(MATCH_TYPE_NEAREST)
                .replyText(buildSlotMatchReply(queryVO.getSpotName(), nearestSlot, requiredCount, false, targetTime))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentReservationOrderResultVO createAgentReservationOrder(AgentReservationOrderDTO dto) {
        SpotReservationCreateDTO createDTO = new SpotReservationCreateDTO();
        if (dto != null) {
            BeanUtils.copyProperties(dto, createDTO);
        }
        SysNormalUser user = requireActiveUser(createDTO.getUserId());
        createDTO.setContactName(resolveAgentContactName(user));
        createDTO.setContactPhone(user.getPhone());
        // Agent专属接口固定来源，避免工具调用方传错统计口径。
        createDTO.setSourceType(SOURCE_AGENT);
        SpotReservationOrderVO orderVO = createOrder(createDTO);
        return AgentReservationOrderResultVO.builder()
                .success(true)
                .reservationNo(orderVO.getReservationNo())
                .status(orderVO.getStatus())
                .scenicAreaId(orderVO.getScenicAreaId())
                .scenicName(orderVO.getScenicName())
                .spotId(orderVO.getSpotId())
                .spotName(orderVO.getSpotName())
                .slotId(orderVO.getSlotId())
                .visitDate(orderVO.getVisitDate())
                .startTime(orderVO.getStartTime())
                .endTime(orderVO.getEndTime())
                .visitorCount(orderVO.getVisitorCount())
                .replyText(buildOrderSuccessReply(orderVO))
                .build();
    }

    @Override
    public List<SpotReservationOrderVO> listAgentRecentOrders(Long userId, String status, Integer limit) {
        if (userId == null) {
            throw new BaseException("用户ID不能为空");
        }
        PageHelper.startPage(1, normalizeAgentLimit(limit, 5));
        Page<SpotReservationOrder> page = orderMapper.pageQuery(null, null, userId, null, normalizeBlank(status), null, null);
        return page.getResult().stream().map(this::buildOrderVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentReservationCancelResultVO cancelAgentReservationOrder(String reservationNo, SpotReservationCancelDTO dto) {
        Boolean success = cancelOrder(reservationNo, dto);
        String replyText = success ? String.format(Locale.CHINA, "已为你取消预约，预约编号 %s。", reservationNo) : "预约取消失败。";
        return AgentReservationCancelResultVO.builder()
                .success(success)
                .reservationNo(reservationNo)
                .replyText(replyText)
                .build();
    }

    @Override
    public AgentReservationSlotRecommendVO recommendAgentReservationSlots(Long spotId, LocalDate startDate, Integer days, LocalTime targetTime, Integer visitorCount, Integer limit) {
        if (spotId == null || startDate == null) {
            throw new BaseException("景点ID和开始日期不能为空");
        }
        int requiredCount = visitorCount == null ? 1 : visitorCount;
        if (requiredCount <= 0) {
            throw new BaseException("预约人数必须大于0");
        }
        ScenicSpot spot = requireSpot(spotId);
        validateSpotReservationEnabled(spot);

        int queryDays = normalizeAgentDays(days);
        int resultLimit = normalizeAgentLimit(limit, 5);
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(defaultNumber(spot.getAdvanceReservationDays(), 7));
        LocalDate beginDate = startDate.isBefore(today) ? today : startDate;
        List<SpotReservationSlotVO> recommendedSlots = new ArrayList<>();

        for (int offset = 0; offset < queryDays && recommendedSlots.size() < resultLimit; offset++) {
            LocalDate visitDate = beginDate.plusDays(offset);
            if (visitDate.isAfter(maxDate)) {
                break;
            }
            List<SpotReservationSlotVO> dateSlots = slotMapper.listBySpotAndDate(spotId, visitDate).stream()
                    .map(slot -> buildSlotVO(slot, true))
                    .filter(slot -> Boolean.TRUE.equals(slot.getAvailable()))
                    .filter(slot -> defaultNumber(slot.getRemainingCount(), 0) >= requiredCount)
                    .sorted(buildRecommendSlotComparator(targetTime))
                    .collect(Collectors.toList());
            for (SpotReservationSlotVO slot : dateSlots) {
                if (recommendedSlots.size() >= resultLimit) {
                    break;
                }
                recommendedSlots.add(slot);
            }
        }

        return AgentReservationSlotRecommendVO.builder()
                .recommendedSlots(recommendedSlots)
                .replyText(buildRecommendSlotsReply(spot.getSpotName(), recommendedSlots, requiredCount))
                .build();
    }

    private List<ReservationDashboardVO.ScenicAreaOptionVO> buildScenicAreaOptions() {
        return scenicAreaMapper.listAllActive().stream()
                .map(area -> ReservationDashboardVO.ScenicAreaOptionVO.builder()
                        .id(area.getId())
                        .name(area.getScenicName())
                        .build())
                .collect(Collectors.toList());
    }

    private ReservationDashboardVO.SummaryVO buildDashboardSummary(ReservationDashboardVO.TrendVO todaySummary,
                                                                   ReservationDashboardVO.TrendVO yesterdaySummary,
                                                                   List<ReservationDashboardVO.CapacitySpotVO> capacitySpots,
                                                                   List<ReservationDashboardVO.StatusDistributionVO> statusDistribution) {
        int orderCount = getOrderCount(todaySummary);
        int visitorCount = getVisitorCount(todaySummary);
        int totalCapacity = capacitySpots.stream().mapToInt(item -> defaultNumber(item.getTotalCapacity(), 0)).sum();
        int reservedCount = capacitySpots.stream().mapToInt(item -> defaultNumber(item.getReservedCount(), 0)).sum();
        long tightSpotCount = capacitySpots.stream().filter(item -> defaultDouble(item.getUsageRate()) >= 80D).count();
        double cancelRate = statusDistribution.stream()
                .filter(item -> STATUS_CANCELLED.equals(item.getStatus()))
                .findFirst()
                .map(item -> defaultDouble(item.getRate()))
                .orElse(0D);

        return ReservationDashboardVO.SummaryVO.builder()
                .orderCount(orderCount)
                .orderCompareText(buildCompareText(orderCount, getOrderCount(yesterdaySummary)))
                .visitorCount(visitorCount)
                .visitorHint(visitorCount > 0 ? "今日预约游客持续入园" : "今日暂无预约游客")
                .capacityUsageRate(calculateRate(reservedCount, totalCapacity))
                .capacityHint(tightSpotCount > 0 ? tightSpotCount + " 个景点偏紧" : "整体容量充足")
                .cancelRate(cancelRate)
                .cancelHint(cancelRate >= 10D ? "取消率偏高，请关注异常订单" : "取消率处于平稳区间")
                .build();
    }

    private List<ReservationDashboardVO.SourceDistributionVO> buildSourceDistribution(List<ReservationDashboardVO.SourceDistributionVO> rows,
                                                                                     int totalOrderCount) {
        Map<String, Integer> countMap = new HashMap<>();
        for (ReservationDashboardVO.SourceDistributionVO row : rows) {
            countMap.put(row.getSourceType(), defaultNumber(row.getOrderCount(), 0));
        }
        List<ReservationDashboardVO.SourceDistributionVO> result = new ArrayList<>();
        for (String sourceType : List.of("FRONTEND", "AGENT", "ADMIN")) {
            int orderCount = countMap.getOrDefault(sourceType, 0);
            result.add(ReservationDashboardVO.SourceDistributionVO.builder()
                    .sourceType(sourceType)
                    .sourceName(resolveSourceName(sourceType))
                    .orderCount(orderCount)
                    .rate(calculateRate(orderCount, totalOrderCount))
                    .build());
        }
        return result;
    }

    private List<ReservationDashboardVO.StatusDistributionVO> buildStatusDistribution(List<ReservationDashboardVO.StatusDistributionVO> rows,
                                                                                     int totalOrderCount) {
        Map<String, Integer> countMap = new HashMap<>();
        for (ReservationDashboardVO.StatusDistributionVO row : rows) {
            countMap.put(row.getStatus(), defaultNumber(row.getOrderCount(), 0));
        }
        List<ReservationDashboardVO.StatusDistributionVO> result = new ArrayList<>();
        for (String status : List.of(STATUS_CONFIRMED, STATUS_PENDING, STATUS_COMPLETED, STATUS_CANCELLED, STATUS_EXPIRED)) {
            int orderCount = countMap.getOrDefault(status, 0);
            result.add(ReservationDashboardVO.StatusDistributionVO.builder()
                    .status(status)
                    .statusName(resolveStatusName(status))
                    .color(resolveStatusColor(status))
                    .orderCount(orderCount)
                    .rate(calculateRate(orderCount, totalOrderCount))
                    .build());
        }
        return result;
    }

    private List<ReservationDashboardVO.HeatSpotVO> buildHeatSpots(List<ReservationDashboardVO.CapacitySpotVO> capacitySpots) {
        List<ReservationDashboardVO.HeatSpotVO> result = new ArrayList<>();
        for (int i = 0; i < capacitySpots.size(); i++) {
            ReservationDashboardVO.CapacitySpotVO item = capacitySpots.get(i);
            // 前端热力图当前使用百分比坐标，后续接入地图边界后可替换为真实经纬度换算。
            double x = 18D + (i % 4) * 21D;
            double y = 22D + (i / 4) * 20D;
            result.add(ReservationDashboardVO.HeatSpotVO.builder()
                    .spotId(item.getSpotId())
                    .spotName(item.getSpotName())
                    .x(Math.min(x, 88D))
                    .y(Math.min(y, 86D))
                    .level(resolveCapacityLevel(item.getUsageRate()))
                    .totalCapacity(defaultNumber(item.getTotalCapacity(), 0))
                    .reservedCount(defaultNumber(item.getReservedCount(), 0))
                    .remainingCount(defaultNumber(item.getRemainingCount(), 0))
                    .usageRate(defaultDouble(item.getUsageRate()))
                    .build());
        }
        return result;
    }

    private List<ReservationDashboardVO.TrendVO> buildDashboardTrend(Long scenicAreaId, LocalDate statisticDate) {
        LocalDate startDate = statisticDate.minusDays(6);
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = statisticDate.plusDays(1).atStartOfDay();
        Map<LocalDate, ReservationDashboardVO.TrendVO> rowMap = new HashMap<>();
        for (ReservationDashboardVO.TrendVO row : orderMapper.listDashboardTrend(scenicAreaId, startTime, endTime)) {
            rowMap.put(row.getDate(), row);
        }
        List<ReservationDashboardVO.TrendVO> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            ReservationDashboardVO.TrendVO row = rowMap.get(date);
            result.add(ReservationDashboardVO.TrendVO.builder()
                    .date(date)
                    .label(date.format(DASHBOARD_TREND_LABEL))
                    .orderCount(row == null ? 0 : getOrderCount(row))
                    .visitorCount(row == null ? 0 : getVisitorCount(row))
                    .build());
        }
        return result;
    }

    private List<ReservationDashboardVO.PeakTimeVO> buildPeakTimes(List<ReservationDashboardVO.PeakTimeVO> rows) {
        return rows.stream().map(row -> ReservationDashboardVO.PeakTimeVO.builder()
                        .startTime(row.getStartTime())
                        .endTime(row.getEndTime())
                        .timeRange(row.getTimeRange())
                        .visitorCount(defaultNumber(row.getVisitorCount(), 0))
                        .note(defaultNumber(row.getVisitorCount(), 0) > 0 ? "该时段预约游客较集中" : "该时段暂无预约")
                        .level(resolveVisitorLevel(defaultNumber(row.getVisitorCount(), 0)))
                        .build())
                .collect(Collectors.toList());
    }

    private List<ReservationDashboardVO.HotSpotRankVO> buildHotSpotRanks(List<ReservationDashboardVO.CapacitySpotVO> capacitySpots) {
        return capacitySpots.stream().limit(5).map(item -> ReservationDashboardVO.HotSpotRankVO.builder()
                        .spotId(item.getSpotId())
                        .spotName(item.getSpotName())
                        .orderCount(defaultNumber(item.getReservedCount(), 0))
                        .usageRate(defaultDouble(item.getUsageRate()))
                        .remainingCount(defaultNumber(item.getRemainingCount(), 0))
                        .level(resolveCapacityLevel(item.getUsageRate()))
                        .note(String.format(Locale.CHINA, "容量利用率 %.1f%%，剩余 %d", defaultDouble(item.getUsageRate()), defaultNumber(item.getRemainingCount(), 0)))
                        .build())
                .collect(Collectors.toList());
    }

    private List<ReservationDashboardVO.WarningVO> buildDashboardWarnings(List<ReservationDashboardVO.CapacitySpotVO> capacitySpots,
                                                                         List<ReservationDashboardVO.StatusDistributionVO> statusDistribution) {
        List<ReservationDashboardVO.WarningVO> warnings = new ArrayList<>();
        capacitySpots.stream()
                .filter(item -> defaultDouble(item.getUsageRate()) >= 80D)
                .limit(4)
                .forEach(item -> warnings.add(ReservationDashboardVO.WarningVO.builder()
                        .title(item.getSpotName())
                        .level(resolveCapacityLevel(item.getUsageRate()))
                        .tag(defaultDouble(item.getUsageRate()) >= 90D ? "快满" : "偏紧")
                        .description(String.format(Locale.CHINA, "容量利用率 %.1f%%，剩余 %d 个名额，建议调整推荐策略。", defaultDouble(item.getUsageRate()), defaultNumber(item.getRemainingCount(), 0)))
                        .build()));
        double cancelRate = statusDistribution.stream()
                .filter(item -> STATUS_CANCELLED.equals(item.getStatus()))
                .findFirst()
                .map(item -> defaultDouble(item.getRate()))
                .orElse(0D);
        if (cancelRate >= 10D) {
            warnings.add(ReservationDashboardVO.WarningVO.builder()
                    .title("预约取消率偏高")
                    .level(cancelRate >= 20D ? "danger" : "warning")
                    .tag("取消")
                    .description(String.format(Locale.CHINA, "今日取消率 %.1f%%，建议排查集中取消来源。", cancelRate))
                    .build());
        }
        return warnings;
    }

    private List<ReservationDashboardVO.LiveActivityVO> buildLiveActivities(List<SpotReservationOrderVO> orders) {
        return orders.stream().map(order -> {
            boolean cancelled = STATUS_CANCELLED.equals(order.getStatus()) && order.getCancelTime() != null;
            String action = cancelled ? "取消" : "预约";
            return ReservationDashboardVO.LiveActivityVO.builder()
                    .title(maskName(order.getContactName()) + " " + action + defaultString(order.getSpotName(), "景点"))
                    .timeText(buildActivityTimeText(cancelled ? order.getCancelTime() : order.getCreateTime()))
                    .description(String.format(Locale.CHINA, "%d 人，来源：%s，%s",
                            defaultNumber(order.getVisitorCount(), 0),
                            resolveSourceName(order.getSourceType()),
                            formatSlotTime(order.getStartTime(), order.getEndTime())))
                    .build();
        }).collect(Collectors.toList());
    }

    private String buildCompareText(int todayCount, int yesterdayCount) {
        if (yesterdayCount == 0) {
            return todayCount == 0 ? "较昨日 持平" : "较昨日 +100.0%";
        }
        double rate = (todayCount - yesterdayCount) * 100D / yesterdayCount;
        return String.format(Locale.CHINA, "较昨日 %+.1f%%", rate);
    }

    private String buildActivityTimeText(LocalDateTime activityTime) {
        if (activityTime == null) {
            return "";
        }
        LocalDateTime now = LocalDateTime.now();
        long minutes = Duration.between(activityTime, now).toMinutes();
        if (minutes >= 0 && minutes < 1) {
            return "刚刚";
        }
        if (minutes >= 1 && minutes < 60) {
            return minutes + " 分钟前";
        }
        if (activityTime.toLocalDate().equals(now.toLocalDate())) {
            return activityTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        return activityTime.format(DASHBOARD_ACTIVITY_TIME);
    }

    private String resolveCapacityLevel(Double usageRate) {
        double rate = defaultDouble(usageRate);
        if (rate >= 90D) {
            return "danger";
        }
        if (rate >= 80D) {
            return "warning";
        }
        return "normal";
    }

    private String resolveVisitorLevel(Integer visitorCount) {
        int count = defaultNumber(visitorCount, 0);
        if (count >= 300) {
            return "danger";
        }
        if (count >= 100) {
            return "warning";
        }
        return "normal";
    }

    private String resolveSourceName(String sourceType) {
        if (SOURCE_AGENT.equals(sourceType)) {
            return "Agent";
        }
        if ("ADMIN".equals(sourceType)) {
            return "后台";
        }
        return "前台";
    }

    private String resolveStatusName(String status) {
        Map<String, String> statusNames = new LinkedHashMap<>();
        statusNames.put(STATUS_CONFIRMED, "已预约");
        statusNames.put(STATUS_PENDING, "待确认");
        statusNames.put(STATUS_COMPLETED, "已完成");
        statusNames.put(STATUS_CANCELLED, "已取消");
        statusNames.put(STATUS_EXPIRED, "已过期");
        return statusNames.getOrDefault(status, status);
    }

    private String resolveStatusColor(String status) {
        Map<String, String> statusColors = new LinkedHashMap<>();
        statusColors.put(STATUS_CONFIRMED, "#21c9aa");
        statusColors.put(STATUS_PENDING, "#f8b84e");
        statusColors.put(STATUS_COMPLETED, "#50d5ff");
        statusColors.put(STATUS_CANCELLED, "#ff6678");
        statusColors.put(STATUS_EXPIRED, "#8b95a7");
        return statusColors.getOrDefault(status, "#8b95a7");
    }

    private String maskName(String name) {
        if (!StringUtils.hasText(name)) {
            return "游客";
        }
        String trimmed = name.trim();
        return trimmed.substring(0, 1) + "**";
    }

    private int getOrderCount(ReservationDashboardVO.TrendVO summary) {
        return summary == null ? 0 : defaultNumber(summary.getOrderCount(), 0);
    }

    private int getVisitorCount(ReservationDashboardVO.TrendVO summary) {
        return summary == null ? 0 : defaultNumber(summary.getVisitorCount(), 0);
    }

    private double calculateRate(int numerator, int denominator) {
        if (denominator <= 0) {
            return 0D;
        }
        return Math.round(numerator * 1000D / denominator) / 10D;
    }

    private double defaultDouble(Double value) {
        return value == null ? 0D : value;
    }

    private String defaultString(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private void validateRuleCreate(SpotReservationRuleCreateDTO dto) {
        if (dto == null || dto.getScenicAreaId() == null || dto.getSpotId() == null) {
            throw new BaseException("景区ID和景点ID不能为空");
        }
        validateTimeRange(dto.getStartTime(), dto.getEndTime());
        validatePositive(dto.getTotalCapacity(), "预约容量必须大于0");
        validateWeekDays(dto.getWeekDays());
        validatePositive(dto.getAdvanceDays(), "提前生成天数必须大于0");
    }

    private void validateSlotCreate(SpotReservationSlotCreateDTO dto) {
        if (dto == null || dto.getScenicAreaId() == null || dto.getSpotId() == null || dto.getVisitDate() == null) {
            throw new BaseException("预约时段参数不能为空");
        }
        validateTimeRange(dto.getStartTime(), dto.getEndTime());
        validatePositive(dto.getTotalCapacity(), "预约容量必须大于0");
    }

    private void validateOrderCreate(SpotReservationCreateDTO dto) {
        if (dto == null || dto.getUserId() == null || dto.getSlotId() == null) {
            throw new BaseException("预约参数不能为空");
        }
        if (dto.getVisitorCount() == null || dto.getVisitorCount() <= 0) {
            throw new BaseException("预约人数必须大于0");
        }
        normalizeSourceType(dto.getSourceType());
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new BaseException("预约开始时间必须早于结束时间");
        }
    }

    private void validateWeekDays(String weekDays) {
        if (!StringUtils.hasText(weekDays)) {
            return;
        }
        for (String item : weekDays.split(",")) {
            String value = item.trim();
            if (!Set.of("1", "2", "3", "4", "5", "6", "7").contains(value)) {
                throw new BaseException("适用星期只能包含1到7");
            }
        }
    }

    private void validatePositive(Integer value, String message) {
        if (value != null && value <= 0) {
            throw new BaseException(message);
        }
    }

    private void validateStatus(Integer status) {
        if (status != null && status != 0 && status != 1) {
            throw new BaseException("状态只能是0或1");
        }
    }

    private ScenicSpot requireSpot(Long id) {
        if (id == null) {
            throw new BaseException("景点ID不能为空");
        }
        ScenicSpot spot = scenicSpotMapper.getById(id);
        if (spot == null) {
            throw new BaseException("景点不存在");
        }
        return spot;
    }

    private SpotReservationRule requireRule(Long id) {
        SpotReservationRule rule = ruleMapper.getById(id);
        if (rule == null) {
            throw new BaseException("预约规则不存在");
        }
        return rule;
    }

    private SpotReservationSlot requireSlot(Long id) {
        if (id == null) {
            throw new BaseException("预约时段ID不能为空");
        }
        SpotReservationSlot slot = slotMapper.getById(id);
        if (slot == null) {
            throw new BaseException("预约时段不存在");
        }
        return slot;
    }

    private SysNormalUser requireActiveUser(Long id) {
        SysNormalUser user = sysNormalUserMapper.getById(id);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted()) || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new BaseException("用户不存在或状态异常");
        }
        return user;
    }

    private void validateSpotArea(Long scenicAreaId, ScenicSpot spot) {
        if (!scenicAreaId.equals(spot.getScenicAreaId())) {
            throw new BaseException("景区ID必须与景点所属景区一致");
        }
    }

    private void validateSpotReservationEnabled(ScenicSpot spot) {
        if (!Integer.valueOf(1).equals(spot.getStatus()) || Integer.valueOf(1).equals(spot.getDeleted())) {
            throw new BaseException("景点不存在或未启用");
        }
        if (!Integer.valueOf(1).equals(spot.getReservationEnabled())) {
            throw new BaseException("景点不支持预约");
        }
    }

    private void validateAdvanceDate(ScenicSpot spot, LocalDate visitDate) {
        int advanceDays = defaultNumber(spot.getAdvanceReservationDays(), 7);
        LocalDate maxDate = LocalDate.now().plusDays(advanceDays);
        if (visitDate.isBefore(LocalDate.now()) || visitDate.isAfter(maxDate)) {
            throw new BaseException("预约日期超出可预约范围");
        }
    }

    private void validateMinAdvanceTime(ScenicSpot spot, SpotReservationSlot slot) {
        int minAdvanceMinutes = defaultNumber(spot.getMinAdvanceMinutes(), 30);
        LocalDateTime earliest = LocalDateTime.now().plusMinutes(minAdvanceMinutes);
        if (LocalDateTime.of(slot.getVisitDate(), slot.getStartTime()).isBefore(earliest)) {
            throw new BaseException("当前时段不满足最少提前预约时间");
        }
    }

    private boolean matchesWeekDays(String weekDays, LocalDate visitDate) {
        if (!StringUtils.hasText(weekDays)) {
            return true;
        }
        String dayValue = String.valueOf(visitDate.getDayOfWeek().getValue());
        for (String item : weekDays.split(",")) {
            if (dayValue.equals(item.trim())) {
                return true;
            }
        }
        return false;
    }

    private SpotReservationRuleVO buildRuleVO(SpotReservationRule rule) {
        if (rule == null) {
            return null;
        }
        ScenicSpot spot = scenicSpotMapper.getById(rule.getSpotId());
        return SpotReservationRuleVO.builder().id(rule.getId()).scenicAreaId(rule.getScenicAreaId()).spotId(rule.getSpotId()).spotName(spot == null ? null : spot.getSpotName()).startTime(rule.getStartTime()).endTime(rule.getEndTime()).totalCapacity(rule.getTotalCapacity()).weekDays(rule.getWeekDays()).advanceDays(rule.getAdvanceDays()).status(rule.getStatus()).remark(rule.getRemark()).createTime(rule.getCreateTime()).updateTime(rule.getUpdateTime()).build();
    }

    private SpotReservationSlotVO buildSlotVO(SpotReservationSlot slot, boolean checkReservationRule) {
        if (slot == null) {
            return null;
        }
        ScenicArea area = scenicAreaMapper.getById(slot.getScenicAreaId());
        ScenicSpot spot = scenicSpotMapper.getById(slot.getSpotId());
        int totalCapacity = defaultNumber(slot.getTotalCapacity(), 0);
        int reservedCount = defaultNumber(slot.getReservedCount(), 0);
        boolean available = Integer.valueOf(1).equals(slot.getStatus()) && reservedCount < totalCapacity;
        if (checkReservationRule && spot != null) {
            available = available && !slot.getVisitDate().isBefore(LocalDate.now()) && !slot.getVisitDate().isAfter(LocalDate.now().plusDays(defaultNumber(spot.getAdvanceReservationDays(), 7))) && !LocalDateTime.of(slot.getVisitDate(), slot.getStartTime()).isBefore(LocalDateTime.now().plusMinutes(defaultNumber(spot.getMinAdvanceMinutes(), 30)));
        }
        return SpotReservationSlotVO.builder().id(slot.getId()).slotId(slot.getId()).scenicAreaId(slot.getScenicAreaId()).scenicName(area == null ? null : area.getScenicName()).spotId(slot.getSpotId()).spotName(spot == null ? null : spot.getSpotName()).ruleId(slot.getRuleId()).visitDate(slot.getVisitDate()).startTime(slot.getStartTime()).endTime(slot.getEndTime()).totalCapacity(totalCapacity).reservedCount(reservedCount).remainingCount(Math.max(totalCapacity - reservedCount, 0)).available(available).status(slot.getStatus()).remark(slot.getRemark()).createTime(slot.getCreateTime()).updateTime(slot.getUpdateTime()).build();
    }

    private SpotReservationOrderVO buildOrderVO(SpotReservationOrder order) {
        if (order == null) {
            return null;
        }
        ScenicArea area = scenicAreaMapper.getById(order.getScenicAreaId());
        ScenicSpot spot = scenicSpotMapper.getById(order.getSpotId());
        SysNormalUser user = sysNormalUserMapper.getById(order.getUserId());
        return SpotReservationOrderVO.builder().id(order.getId()).reservationNo(order.getReservationNo()).userId(order.getUserId()).nickname(user == null ? null : user.getNickname()).scenicAreaId(order.getScenicAreaId()).scenicName(area == null ? null : area.getScenicName()).spotId(order.getSpotId()).spotName(spot == null ? null : spot.getSpotName()).slotId(order.getSlotId()).visitDate(order.getVisitDate()).startTime(order.getStartTime()).endTime(order.getEndTime()).visitorCount(order.getVisitorCount()).contactName(order.getContactName()).contactPhone(order.getContactPhone()).status(order.getStatus()).sourceType(order.getSourceType()).agentSessionCode(order.getAgentSessionCode()).clientRequestId(order.getClientRequestId()).remark(order.getRemark()).cancelReason(order.getCancelReason()).cancelTime(order.getCancelTime()).createTime(order.getCreateTime()).updateTime(order.getUpdateTime()).build();
    }

    private String buildSlotMatchReply(String spotName, SpotReservationSlotVO slot, int visitorCount, boolean exactMatch, LocalTime targetTime) {
        if (slot == null) {
            return null;
        }
        String slotText = formatSlotTime(slot.getStartTime(), slot.getEndTime());
        if (targetTime == null) {
            return String.format(Locale.CHINA, "已找到%s在%s %s的可预约时段，当前剩余%d个名额，可预约%d人。",
                    spotName, slot.getVisitDate().format(AGENT_REPLY_DATE), slotText, slot.getRemainingCount(), visitorCount);
        }
        if (exactMatch) {
            return String.format(Locale.CHINA, "已找到%s在%s %s的可预约时段，当前剩余%d个名额，可预约%d人。",
                    spotName, slot.getVisitDate().format(AGENT_REPLY_DATE), slotText, slot.getRemainingCount(), visitorCount);
        }
        return String.format(Locale.CHINA, "未找到刚好覆盖%s的时段，已为你匹配到最近的可预约时段：%s，当前剩余%d个名额，可预约%d人。",
                targetTime, slotText, slot.getRemainingCount(), visitorCount);
    }

    private String buildOrderSuccessReply(SpotReservationOrderVO orderVO) {
        return String.format(Locale.CHINA, "已为你预约成功：%s，%s %s，%d人，预约编号 %s。",
                orderVO.getSpotName(),
                orderVO.getVisitDate().format(AGENT_REPLY_DATE),
                formatSlotTime(orderVO.getStartTime(), orderVO.getEndTime()),
                orderVO.getVisitorCount(),
                orderVO.getReservationNo());
    }

    private String resolveAgentContactName(SysNormalUser user) {
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname();
        }
        return user.getUsername();
    }

    private String resolveSpotMatchType(String spotName, String keyword) {
        if (!StringUtils.hasText(spotName) || !StringUtils.hasText(keyword)) {
            return "UNKNOWN";
        }
        String normalizedSpotName = spotName.trim();
        String normalizedKeyword = keyword.trim();
        if (normalizedSpotName.equals(normalizedKeyword)) {
            return "NAME_EXACT";
        }
        if (normalizedSpotName.contains(normalizedKeyword)) {
            return "NAME_CONTAINS";
        }
        return "NAME_FUZZY";
    }

    private Double calculateSpotConfidence(String spotName, String keyword) {
        if (!StringUtils.hasText(spotName) || !StringUtils.hasText(keyword)) {
            return 0.0D;
        }
        String normalizedSpotName = spotName.trim();
        String normalizedKeyword = keyword.trim();
        if (normalizedSpotName.equals(normalizedKeyword)) {
            return 1.0D;
        }
        if (normalizedSpotName.contains(normalizedKeyword)) {
            double ratio = (double) normalizedKeyword.length() / normalizedSpotName.length();
            return Math.max(0.7D, Math.min(0.95D, ratio));
        }
        return 0.5D;
    }

    private Comparator<SpotReservationSlotVO> buildRecommendSlotComparator(LocalTime targetTime) {
        Comparator<SpotReservationSlotVO> dateTimeComparator = Comparator
                .comparing(SpotReservationSlotVO::getVisitDate)
                .thenComparing(SpotReservationSlotVO::getStartTime)
                .thenComparing(SpotReservationSlotVO::getSlotId);
        if (targetTime == null) {
            return dateTimeComparator;
        }
        return Comparator
                .comparingLong((SpotReservationSlotVO slot) -> calculateSlotDistanceSeconds(slot, targetTime))
                .thenComparing(dateTimeComparator);
    }

    private String buildRecommendSlotsReply(String spotName, List<SpotReservationSlotVO> slots, int visitorCount) {
        if (slots.isEmpty()) {
            return String.format(Locale.CHINA, "%s近期暂无满足%d人的可预约时段。", spotName, visitorCount);
        }
        SpotReservationSlotVO firstSlot = slots.get(0);
        return String.format(Locale.CHINA, "已为你找到%s在%s %s等%d个可预约时段，可预约%d人。",
                spotName,
                firstSlot.getVisitDate().format(AGENT_REPLY_DATE),
                formatSlotTime(firstSlot.getStartTime(), firstSlot.getEndTime()),
                slots.size(),
                visitorCount);
    }

    private int normalizeAgentDays(Integer days) {
        if (days == null || days < 1) {
            return 7;
        }
        return Math.min(days, 30);
    }

    private int normalizeAgentLimit(Integer limit, int defaultLimit) {
        if (limit == null || limit < 1) {
            return defaultLimit;
        }
        return Math.min(limit, 20);
    }

    private long calculateSlotDistanceSeconds(SpotReservationSlotVO slot, LocalTime targetTime) {
        if (targetTime.isBefore(slot.getStartTime())) {
            return Math.abs(Duration.between(targetTime, slot.getStartTime()).getSeconds());
        }
        if (!targetTime.isBefore(slot.getEndTime())) {
            return Math.abs(Duration.between(slot.getEndTime(), targetTime).getSeconds());
        }
        return 0L;
    }

    private String formatSlotTime(LocalTime startTime, LocalTime endTime) {
        return String.format(Locale.CHINA, "%s-%s", startTime, endTime);
    }

    private String generateReservationNo() {
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        long suffix = Math.abs(System.nanoTime() % 100000);
        return String.format(Locale.ROOT, "YY%s%04d%05d", LocalDate.now().format(RESERVATION_NO_DATE), random, suffix);
    }

    private String normalizeSourceType(String sourceType) {
        String normalized = StringUtils.hasText(sourceType) ? sourceType.trim().toUpperCase(Locale.ROOT) : STATUS_FRONTEND;
        if (!SOURCE_TYPES.contains(normalized)) {
            throw new BaseException("预约来源不支持");
        }
        return normalized;
    }

    private String normalizeBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }

    private Integer defaultNumber(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }
}
