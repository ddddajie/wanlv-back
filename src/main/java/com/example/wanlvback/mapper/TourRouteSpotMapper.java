package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.TourRouteSpot;
import com.example.wanlvback.pojo.vo.RouteSpotDetailVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 路线景点关联数据访问层
 */
public interface TourRouteSpotMapper {

    List<RouteSpotDetailVO> listDetailByRouteId(@Param("routeId") Long routeId);

    int deleteByRouteId(@Param("routeId") Long routeId);

    int batchInsert(@Param("items") List<TourRouteSpot> items);
}
