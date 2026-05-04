package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.SpotReservationOrder;
import com.example.wanlvback.pojo.vo.ReservationDashboardVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface SpotReservationOrderMapper {

    SpotReservationOrder getById(@Param("id") Long id);

    SpotReservationOrder getByReservationNo(@Param("reservationNo") String reservationNo);

    SpotReservationOrder getByClientRequestId(@Param("clientRequestId") String clientRequestId);

    Page<SpotReservationOrder> pageQuery(@Param("scenicAreaId") Long scenicAreaId,
                                         @Param("spotId") Long spotId,
                                         @Param("userId") Long userId,
                                         @Param("visitDate") LocalDate visitDate,
                                         @Param("status") String status,
                                         @Param("sourceType") String sourceType,
                                         @Param("reservationNo") String reservationNo);

    ReservationDashboardVO.TrendVO getDashboardOrderSummary(@Param("scenicAreaId") Long scenicAreaId,
                                                            @Param("startTime") LocalDateTime startTime,
                                                            @Param("endTime") LocalDateTime endTime);

    List<ReservationDashboardVO.SourceDistributionVO> listDashboardSourceDistribution(@Param("scenicAreaId") Long scenicAreaId,
                                                                                     @Param("startTime") LocalDateTime startTime,
                                                                                     @Param("endTime") LocalDateTime endTime);

    List<ReservationDashboardVO.StatusDistributionVO> listDashboardStatusDistribution(@Param("scenicAreaId") Long scenicAreaId,
                                                                                     @Param("startTime") LocalDateTime startTime,
                                                                                     @Param("endTime") LocalDateTime endTime);

    List<ReservationDashboardVO.TrendVO> listDashboardTrend(@Param("scenicAreaId") Long scenicAreaId,
                                                           @Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime);

    List<SpotReservationOrder> listDashboardLiveOrders(@Param("scenicAreaId") Long scenicAreaId,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime,
                                                       @Param("limit") Integer limit);

    int insert(SpotReservationOrder order);

    int cancelByReservationNo(@Param("reservationNo") String reservationNo,
                              @Param("userId") Long userId,
                              @Param("cancelReason") String cancelReason);
}
