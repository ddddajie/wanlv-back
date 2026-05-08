package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.SpotReservationVisitor;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface SpotReservationVisitorMapper {

    int batchInsert(@Param("visitors") List<SpotReservationVisitor> visitors);

    List<SpotReservationVisitor> listByReservationNo(@Param("reservationNo") String reservationNo);

    int countActiveByIdentity(@Param("idCardHash") String idCardHash,
                              @Param("spotId") Long spotId,
                              @Param("visitDate") LocalDate visitDate);

    int cancelByReservationNo(@Param("reservationNo") String reservationNo);

    int expireByOverdueOrders();

    int enterByReservationNo(@Param("reservationNo") String reservationNo);
}
