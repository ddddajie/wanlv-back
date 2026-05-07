package com.example.wanlvback.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotReservationVisitor implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long orderId;
    private String reservationNo;
    private Long userId;
    private Long scenicAreaId;
    private Long spotId;
    private Long slotId;
    private LocalDate visitDate;
    private String realName;
    private String idCardMasked;
    private String idCardHash;
    private Integer booker;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
