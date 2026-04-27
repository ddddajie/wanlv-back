package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.TourRouteGeo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 路线几何数据访问层
 */
public interface TourRouteGeoMapper {

    TourRouteGeo getById(@Param("id") Long id);

    TourRouteGeo getLatestActiveByRouteId(@Param("routeId") Long routeId);

    List<TourRouteGeo> listByRouteId(@Param("routeId") Long routeId);

    int insert(TourRouteGeo tourRouteGeo);

    int updateById(TourRouteGeo tourRouteGeo);
}
