package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.SpotReservationSlot;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SpotReservationSlotMapper {

    SpotReservationSlot getById(@Param("id") Long id);

    SpotReservationSlot getBySpotDateTime(@Param("spotId") Long spotId,
                                          @Param("visitDate") LocalDate visitDate,
                                          @Param("startTime") LocalTime startTime,
                                          @Param("endTime") LocalTime endTime);

    Page<SpotReservationSlot> pageQuery(@Param("scenicAreaId") Long scenicAreaId,
                                        @Param("spotId") Long spotId,
                                        @Param("visitDate") LocalDate visitDate,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("status") Integer status);

    List<SpotReservationSlot> listBySpotAndDate(@Param("spotId") Long spotId,
                                                @Param("visitDate") LocalDate visitDate);

    int insert(SpotReservationSlot slot);

    int updateById(SpotReservationSlot slot);

    int increaseReservedCount(@Param("id") Long id,
                              @Param("visitorCount") Integer visitorCount);

    int decreaseReservedCount(@Param("id") Long id,
                              @Param("visitorCount") Integer visitorCount);
}
