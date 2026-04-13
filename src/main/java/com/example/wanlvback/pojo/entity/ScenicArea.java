package com.example.wanlvback.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 景区实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenicArea implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id; // 主键 ID

    private String scenicName; // 景区名称

    private String scenicCode; // 景区编码

    private String province; // 省份

    private String city; // 城市

    private String district; // 区县

    private String address; // 地址

    private BigDecimal longitude; // 经度

    private BigDecimal latitude; // 纬度

    private String description; // 描述

    private String openingHours; // 开放时间

    private String contactPhone; // 联系电话

    private String coverImageUrl; // 封面图片

    private Integer status; // 状态

    private Integer deleted; // 删除标记

    private Long createBy; // 创建人 ID

    private LocalDateTime createTime; // 创建时间

    private Long updateBy; // 更新人 ID

    private LocalDateTime updateTime; // 更新时间
}
