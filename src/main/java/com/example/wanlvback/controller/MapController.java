package com.example.wanlvback.controller;

import com.example.wanlvback.pojo.dto.MapInteractionLogDTO;
import com.example.wanlvback.pojo.dto.ScenicAreaDTO;
import com.example.wanlvback.pojo.dto.ScenicGeoFeatureDTO;
import com.example.wanlvback.pojo.dto.ScenicSpotDTO;
import com.example.wanlvback.pojo.dto.TourRouteDTO;
import com.example.wanlvback.pojo.dto.TourRouteGeoDTO;
import com.example.wanlvback.pojo.vo.MapInitVO;
import com.example.wanlvback.pojo.vo.RouteDetailVO;
import com.example.wanlvback.pojo.vo.ScenicAreaVO;
import com.example.wanlvback.pojo.vo.ScenicGeoFeatureVO;
import com.example.wanlvback.pojo.vo.ScenicSpotVO;
import com.example.wanlvback.pojo.vo.TourRouteGeoVO;
import com.example.wanlvback.pojo.vo.TourRouteVO;
import com.example.wanlvback.result.PageResult;
import com.example.wanlvback.result.Result;
import com.example.wanlvback.service.MapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 地图业务控制器
 */
@RestController
@RequestMapping("/map")
@Slf4j
public class MapController {

    @Autowired
    private MapService mapService;

    @PostMapping("/scenic-areas")
    public Result<ScenicAreaVO> createScenicArea(@RequestBody ScenicAreaDTO scenicAreaDTO) {
        log.info("收到景区新增请求, scenicName={}", scenicAreaDTO == null ? null : scenicAreaDTO.getScenicName());
        return Result.success(mapService.createScenicArea(scenicAreaDTO));
    }

    @PutMapping("/scenic-areas")
    public Result<ScenicAreaVO> updateScenicArea(@RequestBody ScenicAreaDTO scenicAreaDTO) {
        log.info("收到景区更新请求, id={}", scenicAreaDTO == null ? null : scenicAreaDTO.getId());
        return Result.success(mapService.updateScenicArea(scenicAreaDTO));
    }

    @GetMapping("/scenic-areas/page")
    public Result<PageResult> pageScenicAreas(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(required = false) String scenicName,
                                              @RequestParam(required = false) Integer status) {
        log.info("收到景区分页请求, pageNum={}, pageSize={}, scenicName={}, status={}",
                pageNum, pageSize, scenicName, status);
        return Result.success(mapService.pageScenicAreas(pageNum, pageSize, scenicName, status));
    }

    @GetMapping("/scenic-areas/{id}")
    public Result<ScenicAreaVO> getScenicArea(@PathVariable Long id) {
        log.info("收到景区详情请求, id={}", id);
        return Result.success(mapService.getScenicAreaById(id));
    }

    @PostMapping("/spots")
    public Result<ScenicSpotVO> createScenicSpot(@RequestBody ScenicSpotDTO scenicSpotDTO) {
        log.info("收到景点新增请求, scenicAreaId={}, spotName={}",
                scenicSpotDTO == null ? null : scenicSpotDTO.getScenicAreaId(),
                scenicSpotDTO == null ? null : scenicSpotDTO.getSpotName());
        return Result.success(mapService.createScenicSpot(scenicSpotDTO));
    }

    @PutMapping("/spots")
    public Result<ScenicSpotVO> updateScenicSpot(@RequestBody ScenicSpotDTO scenicSpotDTO) {
        log.info("收到景点更新请求, id={}", scenicSpotDTO == null ? null : scenicSpotDTO.getId());
        return Result.success(mapService.updateScenicSpot(scenicSpotDTO));
    }

    @GetMapping("/spots/page")
    public Result<PageResult> pageScenicSpots(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(required = false) Long scenicAreaId,
                                              @RequestParam(required = false) String spotName,
                                              @RequestParam(required = false) Integer status) {
        log.info("收到景点分页请求, pageNum={}, pageSize={}, scenicAreaId={}, spotName={}, status={}",
                pageNum, pageSize, scenicAreaId, spotName, status);
        return Result.success(mapService.pageScenicSpots(pageNum, pageSize, scenicAreaId, spotName, status));
    }

    @GetMapping("/spots/{id}")
    public Result<ScenicSpotVO> getSpotDetail(@PathVariable Long id) {
        log.info("收到景点详情请求, id={}", id);
        return Result.success(mapService.getScenicSpotDetail(id));
    }

    @PostMapping("/routes")
    public Result<TourRouteVO> createTourRoute(@RequestBody TourRouteDTO tourRouteDTO) {
        log.info("收到路线新增请求, scenicAreaId={}, routeName={}",
                tourRouteDTO == null ? null : tourRouteDTO.getScenicAreaId(),
                tourRouteDTO == null ? null : tourRouteDTO.getRouteName());
        return Result.success(mapService.createTourRoute(tourRouteDTO));
    }

    @PutMapping("/routes")
    public Result<TourRouteVO> updateTourRoute(@RequestBody TourRouteDTO tourRouteDTO) {
        log.info("收到路线更新请求, id={}", tourRouteDTO == null ? null : tourRouteDTO.getId());
        return Result.success(mapService.updateTourRoute(tourRouteDTO));
    }

    @GetMapping("/routes/page")
    public Result<PageResult> pageTourRoutes(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             @RequestParam(required = false) Long scenicAreaId,
                                             @RequestParam(required = false) String routeName,
                                             @RequestParam(required = false) Integer status) {
        log.info("收到路线分页请求, pageNum={}, pageSize={}, scenicAreaId={}, routeName={}, status={}",
                pageNum, pageSize, scenicAreaId, routeName, status);
        return Result.success(mapService.pageTourRoutes(pageNum, pageSize, scenicAreaId, routeName, status));
    }

    @GetMapping("/routes/{id}")
    public Result<RouteDetailVO> getRouteDetail(@PathVariable Long id) {
        log.info("收到路线详情请求, id={}", id);
        return Result.success(mapService.getRouteDetail(id));
    }

    @PostMapping("/route-geos")
    public Result<TourRouteGeoVO> createRouteGeo(@RequestBody TourRouteGeoDTO tourRouteGeoDTO) {
        log.info("收到路线几何新增请求, routeId={}", tourRouteGeoDTO == null ? null : tourRouteGeoDTO.getRouteId());
        return Result.success(mapService.createTourRouteGeo(tourRouteGeoDTO));
    }

    @PutMapping("/route-geos")
    public Result<TourRouteGeoVO> updateRouteGeo(@RequestBody TourRouteGeoDTO tourRouteGeoDTO) {
        log.info("收到路线几何更新请求, id={}", tourRouteGeoDTO == null ? null : tourRouteGeoDTO.getId());
        return Result.success(mapService.updateTourRouteGeo(tourRouteGeoDTO));
    }

    @GetMapping("/route-geos/route/{routeId}")
    public Result<List<TourRouteGeoVO>> listRouteGeos(@PathVariable Long routeId) {
        log.info("收到路线几何列表请求, routeId={}", routeId);
        return Result.success(mapService.listRouteGeos(routeId));
    }

    @PostMapping("/geo-features")
    public Result<ScenicGeoFeatureVO> createGeoFeature(@RequestBody ScenicGeoFeatureDTO scenicGeoFeatureDTO) {
        log.info("收到景区空间要素新增请求, scenicAreaId={}",
                scenicGeoFeatureDTO == null ? null : scenicGeoFeatureDTO.getScenicAreaId());
        return Result.success(mapService.createScenicGeoFeature(scenicGeoFeatureDTO));
    }

    @PutMapping("/geo-features")
    public Result<ScenicGeoFeatureVO> updateGeoFeature(@RequestBody ScenicGeoFeatureDTO scenicGeoFeatureDTO) {
        log.info("收到景区空间要素更新请求, id={}", scenicGeoFeatureDTO == null ? null : scenicGeoFeatureDTO.getId());
        return Result.success(mapService.updateScenicGeoFeature(scenicGeoFeatureDTO));
    }

    @GetMapping("/geo-features")
    public Result<List<ScenicGeoFeatureVO>> listGeoFeatures(@RequestParam Long scenicAreaId) {
        log.info("收到景区空间要素列表请求, scenicAreaId={}", scenicAreaId);
        return Result.success(mapService.listScenicGeoFeatures(scenicAreaId));
    }

    @GetMapping("/init/{scenicAreaId}")
    public Result<MapInitVO> getMapInitData(@PathVariable Long scenicAreaId) {
        log.info("收到地图初始化请求, scenicAreaId={}", scenicAreaId);
        return Result.success(mapService.getMapInitData(scenicAreaId));
    }

    @PostMapping("/interaction-logs")
    public Result<Long> createInteractionLog(@RequestBody MapInteractionLogDTO mapInteractionLogDTO) {
        log.info("收到地图交互日志记录请求, scenicAreaId={}, actionType={}",
                mapInteractionLogDTO == null ? null : mapInteractionLogDTO.getScenicAreaId(),
                mapInteractionLogDTO == null ? null : mapInteractionLogDTO.getActionType());
        return Result.success(mapService.createInteractionLog(mapInteractionLogDTO));
    }
}
