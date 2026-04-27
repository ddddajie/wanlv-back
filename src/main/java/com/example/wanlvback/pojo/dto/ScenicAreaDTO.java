package com.example.wanlvback.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 景区数据传输对象
 */
@Data
public class ScenicAreaDTO implements Serializable {

    private Long id; // 主键ID

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

    private String mapBaseImageUrl; // 景区地图底图URL

    private BigDecimal mapCenterLng; // 地图中心经度

    private BigDecimal mapCenterLat; // 地图中心纬度

    private BigDecimal defaultZoom; // 默认缩放级别

    private BigDecimal minZoom; // 最小缩放级别

    private BigDecimal maxZoom; // 最大缩放级别

    private String mapBoundsJson; // 地图边界JSON

    private Integer status; // 状态
}
