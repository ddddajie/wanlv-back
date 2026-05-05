package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.SpotReservationOrder;
import com.example.wanlvback.pojo.vo.ReservationDashboardVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
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
                                                            @Param("visitDate") LocalDate visitDate);

    List<ReservationDashboardVO.SourceDistributionVO> listDashboardSourceDistribution(@Param("scenicAreaId") Long scenicAreaId,
                                                                                     @Param("visitDate") LocalDate visitDate);

    List<ReservationDashboardVO.StatusDistributionVO> listDashboardStatusDistribution(@Param("scenicAreaId") Long scenicAreaId,
                                                                                     @Param("visitDate") LocalDate visitDate);

    List<ReservationDashboardVO.TrendVO> listDashboardTrend(@Param("scenicAreaId") Long scenicAreaId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);

    List<SpotReservationOrder> listDashboardLiveOrders(@Param("scenicAreaId") Long scenicAreaId,
                                                       @Param("limit") Integer limit);

    int insert(SpotReservationOrder order);

    int cancelByReservationNo(@Param("reservationNo") String reservationNo,
                              @Param("userId") Long userId,
                              @Param("cancelReason") String cancelReason);
}
