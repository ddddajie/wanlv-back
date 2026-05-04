package com.example.wanlvback.service;

import com.example.wanlvback.pojo.dto.SpotReservationCancelDTO;
import com.example.wanlvback.pojo.dto.AgentReservationOrderDTO;
import com.example.wanlvback.pojo.dto.SpotReservationCreateDTO;
import com.example.wanlvback.pojo.dto.SpotReservationRuleCreateDTO;
import com.example.wanlvback.pojo.dto.SpotReservationRuleStatusDTO;
import com.example.wanlvback.pojo.dto.SpotReservationRuleUpdateDTO;
import com.example.wanlvback.pojo.dto.SpotReservationSlotCreateDTO;
import com.example.wanlvback.pojo.dto.SpotReservationSlotGenerateDTO;
import com.example.wanlvback.pojo.dto.SpotReservationSlotUpdateDTO;
import com.example.wanlvback.pojo.vo.AgentReservationOrderResultVO;
import com.example.wanlvback.pojo.vo.AgentReservationCancelResultVO;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SpotReservationService {

    SpotReservationRuleVO createRule(SpotReservationRuleCreateDTO dto);

    SpotReservationRuleVO updateRule(Long id, SpotReservationRuleUpdateDTO dto);

    Boolean updateRuleStatus(Long id, SpotReservationRuleStatusDTO dto);

    PageResult pageRules(Integer pageNum, Integer pageSize, Long scenicAreaId, Long spotId, Integer status);

    SpotReservationGenerateVO generateSlots(SpotReservationSlotGenerateDTO dto);

    SpotReservationSlotVO createSlot(SpotReservationSlotCreateDTO dto);

    SpotReservationSlotVO updateSlot(Long id, SpotReservationSlotUpdateDTO dto);

    PageResult pageSlots(Integer pageNum, Integer pageSize, Long scenicAreaId, Long spotId,
                         LocalDate visitDate, LocalDate startDate, LocalDate endDate, Integer status);

    PageResult pageOrders(Integer pageNum, Integer pageSize, Long scenicAreaId, Long spotId, Long userId,
                          LocalDate visitDate, String status, String sourceType, String reservationNo);

    ReservationDashboardVO getAdminDashboard(Long scenicAreaId, LocalDate date);

    List<ReservationEnabledSpotVO> listReservationEnabledSpots(Long scenicAreaId, String keyword);

    SpotReservationSlotsQueryVO listAvailableSlots(Long spotId, LocalDate visitDate);

    SpotReservationOrderVO createOrder(SpotReservationCreateDTO dto);

    PageResult pageMyOrders(Integer pageNum, Integer pageSize, Long userId, String status);

    Boolean cancelOrder(String reservationNo, SpotReservationCancelDTO dto);

    List<AgentReservationSpotVO> searchAgentReservationSpots(Long scenicAreaId, String keyword);

    AgentReservationSlotMatchVO matchAgentReservationSlot(Long spotId, LocalDate visitDate,
                                                         LocalTime targetTime, Integer visitorCount);

    AgentReservationOrderResultVO createAgentReservationOrder(AgentReservationOrderDTO dto);

    List<SpotReservationOrderVO> listAgentRecentOrders(Long userId, String status, Integer limit);

    AgentReservationCancelResultVO cancelAgentReservationOrder(String reservationNo, SpotReservationCancelDTO dto);

    AgentReservationSlotRecommendVO recommendAgentReservationSlots(Long spotId, LocalDate startDate,
                                                                  Integer days, LocalTime targetTime,
                                                                  Integer visitorCount, Integer limit);
}
