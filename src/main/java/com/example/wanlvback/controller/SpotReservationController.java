package com.example.wanlvback.controller;

import com.example.wanlvback.pojo.dto.AgentReservationOrderDTO;
import com.example.wanlvback.pojo.dto.SpotReservationCancelDTO;
import com.example.wanlvback.pojo.dto.SpotReservationCreateDTO;
import com.example.wanlvback.pojo.dto.SpotReservationRuleCreateDTO;
import com.example.wanlvback.pojo.dto.SpotReservationRuleStatusDTO;
import com.example.wanlvback.pojo.dto.SpotReservationRuleUpdateDTO;
import com.example.wanlvback.pojo.dto.SpotReservationSlotCreateDTO;
import com.example.wanlvback.pojo.dto.SpotReservationSlotGenerateDTO;
import com.example.wanlvback.pojo.dto.SpotReservationSlotUpdateDTO;
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
import com.example.wanlvback.result.Result;
import com.example.wanlvback.service.SpotReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/reservation")
public class SpotReservationController {
    @Autowired
    private SpotReservationService spotReservationService;

    /**
     * 新增景点预约规则。
     */
    @PostMapping("/admin/rules")
    public Result<SpotReservationRuleVO> createRule(@RequestBody SpotReservationRuleCreateDTO dto) {
        log.info("收到预约规则新增请求，spotId={}", dto == null ? null : dto.getSpotId());
        return Result.success(spotReservationService.createRule(dto));
    }

    /**
     * 更新指定景点预约规则。
     */
    @PutMapping("/admin/rules/{id}")
    public Result<SpotReservationRuleVO> updateRule(@PathVariable Long id,
                                                    @RequestBody SpotReservationRuleUpdateDTO dto) {
        log.info("收到预约规则更新请求，id={}", id);
        return Result.success(spotReservationService.updateRule(id, dto));
    }

    /**
     * 更新指定景点预约规则状态。
     */
    @PutMapping("/admin/rules/{id}/status")
    public Result<Boolean> updateRuleStatus(@PathVariable Long id,
                                            @RequestBody SpotReservationRuleStatusDTO dto) {
        log.info("收到预约规则状态更新请求，id={}, status={}", id, dto == null ? null : dto.getStatus());
        return Result.success(spotReservationService.updateRuleStatus(id, dto));
    }

    /**
     * 分页查询景点预约规则。
     */
    @GetMapping("/admin/rules")
    public Result<PageResult> pageRules(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @RequestParam(required = false) Long scenicAreaId,
                                        @RequestParam(required = false) Long spotId,
                                        @RequestParam(required = false) Integer status) {
        log.info("收到预约规则分页查询请求，pageNum={}, pageSize={}, scenicAreaId={}, spotId={}, status={}",
                pageNum, pageSize, scenicAreaId, spotId, status);
        return Result.success(spotReservationService.pageRules(pageNum, pageSize, scenicAreaId, spotId, status));
    }

    /**
     * 根据预约规则批量生成预约时段。
     */
    @PostMapping("/admin/slots/generate")
    public Result<SpotReservationGenerateVO> generateSlots(@RequestBody(required = false) SpotReservationSlotGenerateDTO dto) {
        log.info("收到预约时段生成请求，scenicAreaId={}, spotId={}",
                dto == null ? null : dto.getScenicAreaId(), dto == null ? null : dto.getSpotId());
        return Result.success(spotReservationService.generateSlots(dto));
    }

    /**
     * 手动新增预约时段。
     */
    @PostMapping("/admin/slots")
    public Result<SpotReservationSlotVO> createSlot(@RequestBody SpotReservationSlotCreateDTO dto) {
        log.info("收到预约时段新增请求，spotId={}, visitDate={}",
                dto == null ? null : dto.getSpotId(), dto == null ? null : dto.getVisitDate());
        return Result.success(spotReservationService.createSlot(dto));
    }

    /**
     * 更新指定预约时段。
     */
    @PutMapping("/admin/slots/{id}")
    public Result<SpotReservationSlotVO> updateSlot(@PathVariable Long id,
                                                    @RequestBody SpotReservationSlotUpdateDTO dto) {
        log.info("收到预约时段更新请求，id={}", id);
        return Result.success(spotReservationService.updateSlot(id, dto));
    }

    /**
     * 分页查询预约时段。
     */
    @GetMapping("/admin/slots")
    public Result<PageResult> pageSlots(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @RequestParam(required = false) Long scenicAreaId,
                                        @RequestParam(required = false) Long spotId,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate visitDate,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                        @RequestParam(required = false) Integer status) {
        log.info("收到预约时段分页查询请求，pageNum={}, pageSize={}, scenicAreaId={}, spotId={}, visitDate={}, startDate={}, endDate={}, status={}",
                pageNum, pageSize, scenicAreaId, spotId, visitDate, startDate, endDate, status);
        return Result.success(spotReservationService.pageSlots(pageNum, pageSize, scenicAreaId, spotId,
                visitDate, startDate, endDate, status));
    }

    /**
     * 分页查询预约订单。
     */
    @GetMapping("/admin/orders")
    public Result<PageResult> pageOrders(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) Long scenicAreaId,
                                         @RequestParam(required = false) Long spotId,
                                         @RequestParam(required = false) Long userId,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate visitDate,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) String sourceType,
                                         @RequestParam(required = false) String reservationNo) {
        log.info("收到预约订单分页查询请求，pageNum={}, pageSize={}, scenicAreaId={}, spotId={}, userId={}, visitDate={}, status={}, sourceType={}, reservationNo={}",
                pageNum, pageSize, scenicAreaId, spotId, userId, visitDate, status, sourceType, reservationNo);
        return Result.success(spotReservationService.pageOrders(pageNum, pageSize, scenicAreaId, spotId, userId,
                visitDate, status, sourceType, reservationNo));
    }

    /**
     * 查询预约运营看板聚合数据。
     */
    @GetMapping("/admin/dashboard")
    public Result<ReservationDashboardVO> getAdminDashboard(@RequestParam(required = false) Long scenicAreaId,
                                                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        log.info("收到预约运营看板查询请求，scenicAreaId={}, date={}", scenicAreaId, date);
        return Result.success(spotReservationService.getAdminDashboard(scenicAreaId, date));
    }

    /**
     * 查询已开启预约的景点列表。
     */
    @GetMapping("/spots/enabled")
    public Result<List<ReservationEnabledSpotVO>> listReservationEnabledSpots(@RequestParam(required = false) Long scenicAreaId,
                                                                              @RequestParam(required = false) String keyword) {
        log.info("收到可预约景点列表查询请求，scenicAreaId={}, keyword={}", scenicAreaId, keyword);
        return Result.success(spotReservationService.listReservationEnabledSpots(scenicAreaId, keyword));
    }

    /**
     * Agent根据景点名称搜索可预约景点。
     */
    @GetMapping("/agent/spots/search")
    public Result<List<AgentReservationSpotVO>> searchAgentReservationSpots(@RequestParam String keyword,
                                                                            @RequestParam(required = false) Long scenicAreaId) {
        log.info("收到Agent可预约景点搜索请求，scenicAreaId={}, keyword={}", scenicAreaId, keyword);
        return Result.success(spotReservationService.searchAgentReservationSpots(scenicAreaId, keyword));
    }

    /**
     * Agent查询指定时间附近的可预约时段。
     */
    @GetMapping("/agent/slots/match")
    public Result<AgentReservationSlotMatchVO> matchAgentReservationSlot(@RequestParam Long spotId,
                                                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate visitDate,
                                                                         @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm:ss") LocalTime targetTime,
                                                                         @RequestParam(required = false, defaultValue = "1") Integer visitorCount) {
        log.info("收到Agent预约时段匹配请求，spotId={}, visitDate={}, targetTime={}, visitorCount={}",
                spotId, visitDate, targetTime, visitorCount);
        return Result.success(spotReservationService.matchAgentReservationSlot(spotId, visitDate, targetTime, visitorCount));
    }

    /**
     * Agent查询用户近期预约订单。
     */
    @GetMapping("/agent/orders/recent")
    public Result<List<SpotReservationOrderVO>> listAgentRecentOrders(@RequestParam Long userId,
                                                                      @RequestParam(required = false) String status,
                                                                      @RequestParam(required = false, defaultValue = "5") Integer limit) {
        log.info("收到Agent近期预约订单查询请求，userId={}, status={}, limit={}", userId, status, limit);
        return Result.success(spotReservationService.listAgentRecentOrders(userId, status, limit));
    }

    /**
     * Agent推荐可预约时段。
     */
    @GetMapping("/agent/slots/recommend")
    public Result<AgentReservationSlotRecommendVO> recommendAgentReservationSlots(@RequestParam Long spotId,
                                                                                 @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                                                 @RequestParam(required = false, defaultValue = "7") Integer days,
                                                                                 @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm:ss") LocalTime targetTime,
                                                                                 @RequestParam(required = false, defaultValue = "1") Integer visitorCount,
                                                                                 @RequestParam(required = false, defaultValue = "5") Integer limit) {
        log.info("收到Agent可预约时段推荐请求，spotId={}, startDate={}, days={}, targetTime={}, visitorCount={}, limit={}",
                spotId, startDate, days, targetTime, visitorCount, limit);
        return Result.success(spotReservationService.recommendAgentReservationSlots(spotId, startDate, days, targetTime, visitorCount, limit));
    }

    /**
     * Agent创建预约订单，后端固定sourceType=AGENT。
     */
    @PostMapping("/agent/orders")
    public Result<AgentReservationOrderResultVO> createAgentReservationOrder(@RequestBody AgentReservationOrderDTO dto) {
        log.info("收到Agent预约订单创建请求，userId={}, slotId={}, agentSessionCode={}",
                dto == null ? null : dto.getUserId(),
                dto == null ? null : dto.getSlotId(),
                dto == null ? null : dto.getAgentSessionCode());
        return Result.success(spotReservationService.createAgentReservationOrder(dto));
    }

    /**
     * Agent取消预约订单。
     */
    @PostMapping("/agent/orders/{reservationNo}/cancel")
    public Result<AgentReservationCancelResultVO> cancelAgentReservationOrder(@PathVariable String reservationNo,
                                                                             @RequestBody SpotReservationCancelDTO dto) {
        log.info("收到Agent预约订单取消请求，reservationNo={}, userId={}",
                reservationNo, dto == null ? null : dto.getUserId());
        return Result.success(spotReservationService.cancelAgentReservationOrder(reservationNo, dto));
    }

    /**
     * 查询指定景点某天可预约时段。
     */
    @GetMapping("/slots")
    public Result<SpotReservationSlotsQueryVO> listAvailableSlots(@RequestParam Long spotId,
                                                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate visitDate) {
        log.info("收到可预约时段查询请求，spotId={}, visitDate={}", spotId, visitDate);
        return Result.success(spotReservationService.listAvailableSlots(spotId, visitDate));
    }

    /**
     * 创建用户预约订单。
     */
    @PostMapping("/orders")
    public Result<SpotReservationOrderVO> createOrder(@RequestBody SpotReservationCreateDTO dto) {
        log.info("收到预约订单创建请求，userId={}, slotId={}",
                dto == null ? null : dto.getUserId(), dto == null ? null : dto.getSlotId());
        return Result.success(spotReservationService.createOrder(dto));
    }

    /**
     * 分页查询用户个人预约订单。
     */
    @GetMapping("/orders/my")
    public Result<PageResult> pageMyOrders(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @RequestParam Long userId,
                                           @RequestParam(required = false) String status) {
        log.info("收到我的预约订单分页查询请求，pageNum={}, pageSize={}, userId={}, status={}",
                pageNum, pageSize, userId, status);
        return Result.success(spotReservationService.pageMyOrders(pageNum, pageSize, userId, status));
    }

    /**
     * 取消指定预约订单。
     */
    @PostMapping("/orders/{reservationNo}/cancel")
    public Result<Boolean> cancelOrder(@PathVariable String reservationNo,
                                       @RequestBody SpotReservationCancelDTO dto) {
        log.info("收到预约订单取消请求，reservationNo={}, userId={}",
                reservationNo, dto == null ? null : dto.getUserId());
        return Result.success(spotReservationService.cancelOrder(reservationNo, dto));
    }
}
