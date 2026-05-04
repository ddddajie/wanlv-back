package com.example.wanlvback.service.impl;

import com.example.wanlvback.exception.BaseException;
import com.example.wanlvback.mapper.ScenicAreaMapper;
import com.example.wanlvback.mapper.ScenicSpotMapper;
import com.example.wanlvback.mapper.SpotReservationOrderMapper;
import com.example.wanlvback.mapper.SpotReservationRuleMapper;
import com.example.wanlvback.mapper.SpotReservationSlotMapper;
import com.example.wanlvback.mapper.SysNormalUserMapper;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SpotReservationServiceImpl implements SpotReservationService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_FRONTEND = "FRONTEND";
    private static final DateTimeFormatter RESERVATION_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
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
