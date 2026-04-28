package com.example.wanlvback.service;

import com.example.wanlvback.pojo.dto.MapInteractionLogDTO;
import com.example.wanlvback.pojo.dto.RouteGeoGenerateDTO;
import com.example.wanlvback.pojo.dto.ScenicAreaDTO;
import com.example.wanlvback.pojo.dto.ScenicGeoFeatureDTO;
import com.example.wanlvback.pojo.dto.ScenicSpotDTO;
import com.example.wanlvback.pojo.dto.TourRouteDTO;
import com.example.wanlvback.pojo.dto.TourRouteGeoDTO;
import com.example.wanlvback.pojo.vo.MapInitVO;
import com.example.wanlvback.pojo.vo.RouteDetailVO;
import com.example.wanlvback.pojo.vo.RouteGeoGenerateVO;
import com.example.wanlvback.pojo.vo.ScenicAreaVO;
import com.example.wanlvback.pojo.vo.ScenicGeoFeatureVO;
import com.example.wanlvback.pojo.vo.ScenicSpotVO;
import com.example.wanlvback.pojo.vo.TourRouteGeoVO;
import com.example.wanlvback.pojo.vo.TourRouteVO;
import com.example.wanlvback.result.PageResult;

import java.util.List;

/**
 * 地图业务服务
 */
public interface MapService {

    ScenicAreaVO createScenicArea(ScenicAreaDTO scenicAreaDTO);

    ScenicAreaVO updateScenicArea(ScenicAreaDTO scenicAreaDTO);

    PageResult pageScenicAreas(Integer pageNum, Integer pageSize, String scenicName, Integer status);

    ScenicAreaVO getScenicAreaById(Long id);

    ScenicSpotVO createScenicSpot(ScenicSpotDTO scenicSpotDTO);

    ScenicSpotVO updateScenicSpot(ScenicSpotDTO scenicSpotDTO);

    PageResult pageScenicSpots(Integer pageNum, Integer pageSize, Long scenicAreaId, String spotName, Integer status);

    ScenicSpotVO getScenicSpotDetail(Long id);

    TourRouteVO createTourRoute(TourRouteDTO tourRouteDTO);

    TourRouteVO updateTourRoute(TourRouteDTO tourRouteDTO);

    PageResult pageTourRoutes(Integer pageNum, Integer pageSize, Long scenicAreaId, String routeName, Integer status);

    RouteDetailVO getRouteDetail(Long id);

    TourRouteGeoVO createTourRouteGeo(TourRouteGeoDTO tourRouteGeoDTO);

    RouteGeoGenerateVO generateRouteGeo(Long routeId, RouteGeoGenerateDTO routeGeoGenerateDTO);

    TourRouteGeoVO updateTourRouteGeo(TourRouteGeoDTO tourRouteGeoDTO);

    List<TourRouteGeoVO> listRouteGeos(Long routeId);

    ScenicGeoFeatureVO createScenicGeoFeature(ScenicGeoFeatureDTO scenicGeoFeatureDTO);

    ScenicGeoFeatureVO updateScenicGeoFeature(ScenicGeoFeatureDTO scenicGeoFeatureDTO);

    List<ScenicGeoFeatureVO> listScenicGeoFeatures(Long scenicAreaId);

    MapInitVO getMapInitData(Long scenicAreaId);

    Long createInteractionLog(MapInteractionLogDTO mapInteractionLogDTO);
}
