package com.example.wanlvback.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.wanlvback.exception.BaseException;
import com.example.wanlvback.mapper.AgentRouteGeoMapper;
import com.example.wanlvback.mapper.MapInteractionLogMapper;
import com.example.wanlvback.mapper.ScenicAreaMapper;
import com.example.wanlvback.mapper.ScenicGeoFeatureMapper;
import com.example.wanlvback.mapper.ScenicSpotMapper;
import com.example.wanlvback.mapper.TourRouteGeoMapper;
import com.example.wanlvback.mapper.TourRouteMapper;
import com.example.wanlvback.mapper.TourRouteSpotMapper;
import com.example.wanlvback.pojo.dto.AgentRouteGeoGenerateDTO;
import com.example.wanlvback.pojo.dto.MapInteractionLogDTO;
import com.example.wanlvback.pojo.dto.RouteGeoGenerateDTO;
import com.example.wanlvback.pojo.dto.ScenicAreaDTO;
import com.example.wanlvback.pojo.dto.ScenicGeoFeatureDTO;
import com.example.wanlvback.pojo.dto.ScenicSpotDTO;
import com.example.wanlvback.pojo.dto.TourRouteDTO;
import com.example.wanlvback.pojo.dto.TourRouteGeoDTO;
import com.example.wanlvback.pojo.dto.TourRouteSpotDTO;
import com.example.wanlvback.pojo.entity.AgentRouteGeo;
import com.example.wanlvback.pojo.entity.MapInteractionLog;
import com.example.wanlvback.pojo.entity.ScenicArea;
import com.example.wanlvback.pojo.entity.ScenicGeoFeature;
import com.example.wanlvback.pojo.entity.ScenicSpot;
import com.example.wanlvback.pojo.entity.TourRoute;
import com.example.wanlvback.pojo.entity.TourRouteGeo;
import com.example.wanlvback.pojo.entity.TourRouteSpot;
import com.example.wanlvback.pojo.vo.MapInitVO;
import com.example.wanlvback.pojo.vo.AgentRouteGeoVO;
import com.example.wanlvback.pojo.vo.MapRouteVO;
import com.example.wanlvback.pojo.vo.RouteDetailVO;
import com.example.wanlvback.pojo.vo.RouteGeoGenerateVO;
import com.example.wanlvback.pojo.vo.RouteGeoGenerateWarningVO;
import com.example.wanlvback.pojo.vo.RouteSpotDetailVO;
import com.example.wanlvback.pojo.vo.ScenicAreaVO;
import com.example.wanlvback.pojo.vo.ScenicGeoFeatureVO;
import com.example.wanlvback.pojo.vo.ScenicSpotVO;
import com.example.wanlvback.pojo.vo.TourRouteGeoVO;
import com.example.wanlvback.pojo.vo.TourRouteVO;
import com.example.wanlvback.result.PageResult;
import com.example.wanlvback.service.MapService;
import com.example.wanlvback.utils.AuthUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 地图业务服务实现
 */
@Service
@Slf4j
public class MapServiceImpl implements MapService {

    private static final String FEATURE_TYPE_BOUNDARY = "BOUNDARY";
    private static final String FEATURE_TYPE_ZONE = "ZONE";
    private static final String FEATURE_TYPE_RESTRICTED = "RESTRICTED";
    private static final String FEATURE_TYPE_ENTRANCE_AREA = "ENTRANCE_AREA";
    private static final String FEATURE_TYPE_ROAD = "ROAD";
    private static final String GEOMETRY_TYPE_POINT = "POINT";
    private static final String GEOMETRY_TYPE_LINE = "LINE";
    private static final String GEOMETRY_TYPE_POLYGON = "POLYGON";
    private static final String FALLBACK_DIRECT_SEGMENT = "DIRECT_SEGMENT";
    private static final String FALLBACK_FAIL = "FAIL";
    private static final double DEFAULT_SNAP_TOLERANCE_METERS = 80.0;
    private static final double NODE_MERGE_TOLERANCE_METERS = 3.0;
    private static final double EARTH_RADIUS_METERS = 6371008.8;
    private static final String DEFAULT_AGENT_ROUTE_NAME = "智能定制路线";
    private static final Set<String> SUPPORTED_FEATURE_TYPES = Set.of(
            FEATURE_TYPE_BOUNDARY,
            FEATURE_TYPE_ZONE,
            FEATURE_TYPE_RESTRICTED,
            FEATURE_TYPE_ENTRANCE_AREA,
            FEATURE_TYPE_ROAD
    );

    @Autowired
    private ScenicAreaMapper scenicAreaMapper;

    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    @Autowired
    private TourRouteMapper tourRouteMapper;

    @Autowired
    private TourRouteSpotMapper tourRouteSpotMapper;

    @Autowired
    private TourRouteGeoMapper tourRouteGeoMapper;

    @Autowired
    private ScenicGeoFeatureMapper scenicGeoFeatureMapper;

    @Autowired
    private MapInteractionLogMapper mapInteractionLogMapper;

    @Autowired
    private AgentRouteGeoMapper agentRouteGeoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScenicAreaVO createScenicArea(ScenicAreaDTO scenicAreaDTO) {
        validateScenicAreaForCreate(scenicAreaDTO);

        LocalDateTime now = LocalDateTime.now();
        ScenicArea scenicArea = new ScenicArea();
        BeanUtils.copyProperties(scenicAreaDTO, scenicArea);
        scenicArea.setStatus(defaultStatus(scenicAreaDTO.getStatus()));
        scenicArea.setDeleted(0);
        scenicArea.setCreateTime(now);
        scenicArea.setUpdateTime(now);
        scenicAreaMapper.insert(scenicArea);
        return buildScenicAreaVO(requireScenicArea(scenicArea.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScenicAreaVO updateScenicArea(ScenicAreaDTO scenicAreaDTO) {
        if (scenicAreaDTO == null || scenicAreaDTO.getId() == null) {
            throw new BaseException("景区ID不能为空");
        }
        requireScenicArea(scenicAreaDTO.getId());

        ScenicArea scenicArea = new ScenicArea();
        BeanUtils.copyProperties(scenicAreaDTO, scenicArea);
        scenicAreaMapper.updateById(scenicArea);
        return buildScenicAreaVO(requireScenicArea(scenicAreaDTO.getId()));
    }

    @Override
    public PageResult pageScenicAreas(Integer pageNum, Integer pageSize, String scenicName, Integer status) {
        PageHelper.startPage(normalizePageNum(pageNum), normalizePageSize(pageSize));
        Page<ScenicArea> page = scenicAreaMapper.pageQuery(scenicName, status);
        return new PageResult(page.getTotal(),
                page.getResult().stream().map(this::buildScenicAreaVO).collect(Collectors.toList()));
    }

    @Override
    public ScenicAreaVO getScenicAreaById(Long id) {
        return buildScenicAreaVO(requireScenicArea(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteScenicArea(Long id) {
        requireScenicArea(id);
        tourRouteGeoMapper.disableByScenicAreaId(id);
        tourRouteMapper.logicalDeleteByScenicAreaId(id);
        scenicSpotMapper.logicalDeleteByScenicAreaId(id);
        scenicGeoFeatureMapper.logicalDeleteByScenicAreaId(id);
        scenicAreaMapper.logicalDeleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScenicSpotVO createScenicSpot(ScenicSpotDTO scenicSpotDTO) {
        validateScenicSpotForCreate(scenicSpotDTO);
        requireScenicArea(scenicSpotDTO.getScenicAreaId());

        LocalDateTime now = LocalDateTime.now();
        ScenicSpot scenicSpot = new ScenicSpot();
        BeanUtils.copyProperties(scenicSpotDTO, scenicSpot);
        scenicSpot.setPoiType(defaultIfBlank(scenicSpotDTO.getPoiType(), "SCENIC_SPOT"));
        scenicSpot.setRecommendedLevel(defaultNumber(scenicSpotDTO.getRecommendedLevel(), 0));
        scenicSpot.setSortNo(defaultNumber(scenicSpotDTO.getSortNo(), 0));
        scenicSpot.setStatus(defaultStatus(scenicSpotDTO.getStatus()));
        scenicSpot.setReservationEnabled(defaultNumber(scenicSpotDTO.getReservationEnabled(), 0));
        scenicSpot.setAdvanceReservationDays(defaultNumber(scenicSpotDTO.getAdvanceReservationDays(), 7));
        scenicSpot.setMinAdvanceMinutes(defaultNumber(scenicSpotDTO.getMinAdvanceMinutes(), 30));
        scenicSpot.setDeleted(0);
        scenicSpot.setCreateTime(now);
        scenicSpot.setUpdateTime(now);
        scenicSpotMapper.insert(scenicSpot);
        return buildScenicSpotVO(requireScenicSpot(scenicSpot.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScenicSpotVO updateScenicSpot(ScenicSpotDTO scenicSpotDTO) {
        if (scenicSpotDTO == null || scenicSpotDTO.getId() == null) {
            throw new BaseException("景点ID不能为空");
        }
        requireScenicSpot(scenicSpotDTO.getId());
        if (scenicSpotDTO.getScenicAreaId() != null) {
            requireScenicArea(scenicSpotDTO.getScenicAreaId());
        }

        ScenicSpot scenicSpot = new ScenicSpot();
        BeanUtils.copyProperties(scenicSpotDTO, scenicSpot);
        scenicSpotMapper.updateById(scenicSpot);
        return buildScenicSpotVO(requireScenicSpot(scenicSpotDTO.getId()));
    }

    @Override
    public PageResult pageScenicSpots(Integer pageNum, Integer pageSize, Long scenicAreaId, String spotName, Integer status) {
        PageHelper.startPage(normalizePageNum(pageNum), normalizePageSize(pageSize));
        Page<ScenicSpot> page = scenicSpotMapper.pageQuery(scenicAreaId, spotName, status);
        return new PageResult(page.getTotal(),
                page.getResult().stream().map(this::buildScenicSpotVO).collect(Collectors.toList()));
    }

    @Override
    public ScenicSpotVO getScenicSpotDetail(Long id) {
        if (id == null) {
            throw new BaseException("景点ID不能为空");
        }
        ScenicSpot scenicSpot = scenicSpotMapper.getActiveById(id);
        if (scenicSpot == null) {
            throw new BaseException("景点不存在或未启用");
        }
        return buildScenicSpotVO(scenicSpot);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteScenicSpot(Long id) {
        requireScenicSpot(id);
        List<Long> routeIds = tourRouteSpotMapper.listRouteIdsBySpotId(id);
        for (Long routeId : routeIds) {
            deleteTourRouteCascade(routeId);
        }
        scenicSpotMapper.logicalDeleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TourRouteVO createTourRoute(TourRouteDTO tourRouteDTO) {
        validateRouteForCreate(tourRouteDTO);
        requireScenicArea(tourRouteDTO.getScenicAreaId());

        LocalDateTime now = LocalDateTime.now();
        TourRoute tourRoute = new TourRoute();
        BeanUtils.copyProperties(tourRouteDTO, tourRoute);
        tourRoute.setStatus(defaultStatus(tourRouteDTO.getStatus()));
        tourRoute.setDeleted(0);
        tourRoute.setCreateTime(now);
        tourRoute.setUpdateTime(now);
        tourRouteMapper.insert(tourRoute);

        replaceRouteSpots(tourRoute.getId(), tourRoute.getScenicAreaId(), tourRouteDTO.getRouteSpots());
        return buildTourRouteVO(requireTourRoute(tourRoute.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TourRouteVO updateTourRoute(TourRouteDTO tourRouteDTO) {
        if (tourRouteDTO == null || tourRouteDTO.getId() == null) {
            throw new BaseException("路线ID不能为空");
        }
        TourRoute existRoute = requireTourRoute(tourRouteDTO.getId());
        Long scenicAreaId = tourRouteDTO.getScenicAreaId() == null ? existRoute.getScenicAreaId() : tourRouteDTO.getScenicAreaId();
        requireScenicArea(scenicAreaId);
        if (!scenicAreaId.equals(existRoute.getScenicAreaId())
                && !CollectionUtils.isEmpty(tourRouteGeoMapper.listByRouteId(tourRouteDTO.getId()))) {
            throw new BaseException("路线已有轨迹数据，不能直接修改所属景区");
        }

        TourRoute tourRoute = new TourRoute();
        BeanUtils.copyProperties(tourRouteDTO, tourRoute);
        tourRouteMapper.updateById(tourRoute);

        if (tourRouteDTO.getRouteSpots() != null) {
            replaceRouteSpots(tourRouteDTO.getId(), scenicAreaId, tourRouteDTO.getRouteSpots());
        }
        return buildTourRouteVO(requireTourRoute(tourRouteDTO.getId()));
    }

    @Override
    public PageResult pageTourRoutes(Integer pageNum, Integer pageSize, Long scenicAreaId, String routeName, Integer status) {
        PageHelper.startPage(normalizePageNum(pageNum), normalizePageSize(pageSize));
        Page<TourRoute> page = tourRouteMapper.pageQuery(scenicAreaId, routeName, status);
        return new PageResult(page.getTotal(),
                page.getResult().stream().map(this::buildTourRouteVO).collect(Collectors.toList()));
    }

    @Override
    public RouteDetailVO getRouteDetail(Long id) {
        if (id == null) {
            throw new BaseException("路线ID不能为空");
        }

        TourRoute route = tourRouteMapper.getActiveById(id);
        if (route == null) {
            throw new BaseException("路线不存在或未启用");
        }
        TourRouteGeo routeGeo = tourRouteGeoMapper.getLatestActiveByRouteId(id);
        if (routeGeo != null && routeGeo.getScenicAreaId() != null
                && !route.getScenicAreaId().equals(routeGeo.getScenicAreaId())) {
            routeGeo = null;
        }
        List<RouteSpotDetailVO> spots = tourRouteSpotMapper.listDetailByRouteId(id);
        return RouteDetailVO.builder()
                .route(buildTourRouteVO(route))
                .routeGeo(buildTourRouteGeoVO(routeGeo))
                .spots(spots)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTourRoute(Long id) {
        requireTourRoute(id);
        deleteTourRouteCascade(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TourRouteGeoVO createTourRouteGeo(TourRouteGeoDTO tourRouteGeoDTO) {
        validateRouteGeoForCreate(tourRouteGeoDTO);
        TourRoute route = requireTourRoute(tourRouteGeoDTO.getRouteId());
        validateRouteGeoScenicArea(tourRouteGeoDTO.getScenicAreaId(), route);

        LocalDateTime now = LocalDateTime.now();
        TourRouteGeo tourRouteGeo = new TourRouteGeo();
        BeanUtils.copyProperties(tourRouteGeoDTO, tourRouteGeo);
        tourRouteGeo.setScenicAreaId(route.getScenicAreaId());
        tourRouteGeo.setVersion(resolveGeoVersion(tourRouteGeoDTO));
        tourRouteGeo.setStatus(defaultStatus(tourRouteGeoDTO.getStatus()));
        tourRouteGeo.setCreateTime(now);
        tourRouteGeo.setUpdateTime(now);
        tourRouteGeoMapper.insert(tourRouteGeo);
        return buildTourRouteGeoVO(tourRouteGeoMapper.getById(tourRouteGeo.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RouteGeoGenerateVO generateRouteGeo(Long routeId, RouteGeoGenerateDTO routeGeoGenerateDTO) {
        if (routeId == null) {
            throw new BaseException("路线ID不能为空");
        }
        RouteGeoGenerateDTO request = routeGeoGenerateDTO == null ? new RouteGeoGenerateDTO() : routeGeoGenerateDTO;
        TourRoute route = requireTourRoute(routeId);
        List<RouteSpotDetailVO> spots = tourRouteSpotMapper.listDetailByRouteId(routeId);
        if (CollectionUtils.isEmpty(spots) || spots.size() < 2) {
            throw new BaseException("路线至少需要配置 2 个有效景点后才能自动生成轨迹。");
        }

        Integer version = resolveGeoVersionForGenerate(routeId, request.getVersion());
        GeneratedRouteGeoCalculation calculation = calculateGeneratedRouteGeo(
                routeId, route.getScenicAreaId(), route.getRouteName(), request, spots);
        Long routeGeoId = null;
        boolean saved = Boolean.TRUE.equals(request.getSaveAsVersion());
        if (saved) {
            TourRouteGeo savedGeo = saveGeneratedRouteGeo(routeId, route.getScenicAreaId(), calculation.geojson(), version, request);
            routeGeoId = savedGeo.getId();
            updateRouteDistance(routeId, calculation.distanceMeters());
        }

        return RouteGeoGenerateVO.builder()
                .routeId(routeId)
                .scenicAreaId(route.getScenicAreaId())
                .routeName(route.getRouteName())
                .version(version)
                .saved(saved)
                .routeGeoId(routeGeoId)
                .distanceMeters(roundDouble(calculation.distanceMeters(), 2))
                .spotCount(spots.size())
                .roadSegmentCount(calculation.roadSegmentCount())
                .geojson(calculation.geojson())
                .spots(spots)
                .warnings(calculation.warnings())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateAgentRouteGeo(AgentRouteGeoGenerateDTO agentRouteGeoGenerateDTO) {
        validateAgentRouteGeoGenerate(agentRouteGeoGenerateDTO);

        Long userId = requireAgentRouteUserId(agentRouteGeoGenerateDTO);
        ScenicArea scenicArea = resolveAgentScenicArea(agentRouteGeoGenerateDTO.getScenicName());
        List<RouteSpotDetailVO> spots = buildAgentRouteSpots(agentRouteGeoGenerateDTO, scenicArea);
        RouteGeoGenerateDTO request = buildRouteGeoGenerateRequest(agentRouteGeoGenerateDTO);
        String routeName = defaultIfBlank(agentRouteGeoGenerateDTO.getRouteName(), DEFAULT_AGENT_ROUTE_NAME);
        GeneratedRouteGeoCalculation calculation = calculateGeneratedRouteGeo(
                null, scenicArea.getId(), routeName, request, spots);

        saveAgentRouteGeo(userId, scenicArea.getId(), routeName, calculation, spots);
        return true;
    }

    @Override
    public AgentRouteGeoVO getLatestAgentRouteGeo(Long userId, Long scenicAreaId) {
        AuthUtil.requireSelfOrAdmin(userId);
        if (scenicAreaId == null) {
            throw new BaseException("景区ID不能为空");
        }
        requireScenicArea(scenicAreaId);
        AgentRouteGeo agentRouteGeo = agentRouteGeoMapper.getLatestByUserIdAndScenicAreaId(userId, scenicAreaId);
        return buildAgentRouteGeoVO(agentRouteGeo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TourRouteGeoVO updateTourRouteGeo(TourRouteGeoDTO tourRouteGeoDTO) {
        if (tourRouteGeoDTO == null || tourRouteGeoDTO.getId() == null) {
            throw new BaseException("路线几何数据ID不能为空");
        }
        TourRouteGeo existGeo = tourRouteGeoMapper.getById(tourRouteGeoDTO.getId());
        if (existGeo == null) {
            throw new BaseException("路线几何数据不存在");
        }
        Long routeId = tourRouteGeoDTO.getRouteId() == null ? existGeo.getRouteId() : tourRouteGeoDTO.getRouteId();
        TourRoute route = requireTourRoute(routeId);
        validateRouteGeoScenicArea(tourRouteGeoDTO.getScenicAreaId(), route);

        TourRouteGeo tourRouteGeo = new TourRouteGeo();
        BeanUtils.copyProperties(tourRouteGeoDTO, tourRouteGeo);
        tourRouteGeo.setRouteId(routeId);
        tourRouteGeo.setScenicAreaId(route.getScenicAreaId());
        tourRouteGeoMapper.updateById(tourRouteGeo);
        return buildTourRouteGeoVO(tourRouteGeoMapper.getById(tourRouteGeoDTO.getId()));
    }

    @Override
    public List<TourRouteGeoVO> listRouteGeos(Long routeId) {
        if (routeId == null) {
            throw new BaseException("路线ID不能为空");
        }
        requireTourRoute(routeId);
        return tourRouteGeoMapper.listByRouteId(routeId)
                .stream()
                .map(this::buildTourRouteGeoVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScenicGeoFeatureVO createScenicGeoFeature(ScenicGeoFeatureDTO scenicGeoFeatureDTO) {
        validateGeoFeatureForCreate(scenicGeoFeatureDTO);
        normalizeAndValidateGeoFeature(scenicGeoFeatureDTO);
        requireScenicArea(scenicGeoFeatureDTO.getScenicAreaId());

        LocalDateTime now = LocalDateTime.now();
        ScenicGeoFeature scenicGeoFeature = new ScenicGeoFeature();
        BeanUtils.copyProperties(scenicGeoFeatureDTO, scenicGeoFeature);
        scenicGeoFeature.setStatus(defaultStatus(scenicGeoFeatureDTO.getStatus()));
        scenicGeoFeature.setDeleted(defaultNumber(scenicGeoFeatureDTO.getDeleted(), 0));
        scenicGeoFeature.setCreateTime(now);
        scenicGeoFeature.setUpdateTime(now);
        scenicGeoFeatureMapper.insert(scenicGeoFeature);
        return buildScenicGeoFeatureVO(scenicGeoFeatureMapper.getById(scenicGeoFeature.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScenicGeoFeatureVO updateScenicGeoFeature(ScenicGeoFeatureDTO scenicGeoFeatureDTO) {
        if (scenicGeoFeatureDTO == null || scenicGeoFeatureDTO.getId() == null) {
            throw new BaseException("空间要素ID不能为空");
        }
        ScenicGeoFeature existFeature = scenicGeoFeatureMapper.getById(scenicGeoFeatureDTO.getId());
        if (existFeature == null) {
            throw new BaseException("空间要素不存在");
        }
        if (scenicGeoFeatureDTO.getScenicAreaId() != null) {
            requireScenicArea(scenicGeoFeatureDTO.getScenicAreaId());
        }
        normalizeAndValidateGeoFeatureForUpdate(scenicGeoFeatureDTO, existFeature);

        ScenicGeoFeature scenicGeoFeature = new ScenicGeoFeature();
        BeanUtils.copyProperties(scenicGeoFeatureDTO, scenicGeoFeature);
        scenicGeoFeatureMapper.updateById(scenicGeoFeature);
        return buildScenicGeoFeatureVO(scenicGeoFeatureMapper.getById(scenicGeoFeatureDTO.getId()));
    }

    @Override
    public List<ScenicGeoFeatureVO> listScenicGeoFeatures(Long scenicAreaId) {
        if (scenicAreaId == null) {
            throw new BaseException("景区ID不能为空");
        }
        requireScenicArea(scenicAreaId);
        return scenicGeoFeatureMapper.listByScenicAreaId(scenicAreaId)
                .stream()
                .map(this::buildScenicGeoFeatureVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteScenicGeoFeature(Long id) {
        if (id == null) {
            throw new BaseException("空间要素ID不能为空");
        }
        ScenicGeoFeature existFeature = scenicGeoFeatureMapper.getById(id);
        if (existFeature == null) {
            throw new BaseException("空间要素不存在");
        }
        scenicGeoFeatureMapper.logicalDeleteById(id);
    }

    @Override
    public MapInitVO getMapInitData(Long scenicAreaId) {
        if (scenicAreaId == null) {
            throw new BaseException("景区ID不能为空");
        }
        ScenicArea scenicArea = scenicAreaMapper.getActiveById(scenicAreaId);
        if (scenicArea == null) {
            throw new BaseException("景区不存在或未启用");
        }

        List<ScenicGeoFeatureVO> geoFeatures = scenicGeoFeatureMapper.listActiveByScenicAreaId(scenicAreaId)
                .stream()
                .map(this::buildScenicGeoFeatureVO)
                .collect(Collectors.toList());
        List<ScenicSpotVO> spots = scenicSpotMapper.listActiveByScenicAreaId(scenicAreaId)
                .stream()
                .map(this::buildScenicSpotVO)
                .collect(Collectors.toList());
        List<MapRouteVO> routes = tourRouteMapper.listMapRoutesByScenicAreaId(scenicAreaId);

        return MapInitVO.builder()
                .scenicArea(buildScenicAreaVO(scenicArea))
                .geoFeatures(geoFeatures)
                .spots(spots)
                .routes(routes)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createInteractionLog(MapInteractionLogDTO mapInteractionLogDTO) {
        validateInteractionLog(mapInteractionLogDTO);
        requireScenicArea(mapInteractionLogDTO.getScenicAreaId());

        MapInteractionLog mapInteractionLog = new MapInteractionLog();
        BeanUtils.copyProperties(mapInteractionLogDTO, mapInteractionLog);
        mapInteractionLog.setCreateTime(LocalDateTime.now());
        mapInteractionLogMapper.insert(mapInteractionLog);
        return mapInteractionLog.getId();
    }

    /**
     * 重点：公共轨迹生成只负责计算，不负责保存，避免 Agent 定制路线误入库。
     */
    private GeneratedRouteGeoCalculation calculateGeneratedRouteGeo(Long routeId,
                                                                    Long scenicAreaId,
                                                                    String routeName,
                                                                    RouteGeoGenerateDTO request,
                                                                    List<RouteSpotDetailVO> spots) {
        validateRouteSpotCoordinates(spots);

        double snapToleranceMeters = request.getSnapToleranceMeters() == null
                ? DEFAULT_SNAP_TOLERANCE_METERS
                : request.getSnapToleranceMeters();
        if (snapToleranceMeters < 0) {
            throw new BaseException("景点吸附容忍距离不能小于0");
        }
        String fallbackStrategy = normalizeFallbackStrategy(request.getFallbackStrategy());
        Set<String> roadTypes = normalizeRoadTypes(request.getRoadTypes());
        List<RouteGeoGenerateWarningVO> warnings = new ArrayList<>();
        RoadNetwork roadNetwork = buildRoadNetwork(scenicAreaId, roadTypes, warnings);
        if (roadNetwork.segments.isEmpty()) {
            throw new BaseException("当前景区没有可用于计算的道路");
        }

        List<SnappedSpot> snappedSpots = new ArrayList<>();
        for (RouteSpotDetailVO spot : spots) {
            SnappedSpot snappedSpot = snapSpotToRoad(spot, roadNetwork);
            if (snappedSpot.distanceMeters() > snapToleranceMeters) {
                if (FALLBACK_FAIL.equals(fallbackStrategy)) {
                    throw new BaseException("景点无法吸附到道路，且兜底策略为 FAIL");
                }
                warnings.add(RouteGeoGenerateWarningVO.builder()
                        .code("SPOT_SNAP_DISTANCE_EXCEEDED")
                        .message(String.format(Locale.ROOT, "景点“%s”距离最近道路 %.1fm，超过建议阈值 %.1fm，已吸附到最近道路点。",
                                spot.getSpotName(), snappedSpot.distanceMeters(), snapToleranceMeters))
                        .spotId(spot.getSpotId())
                        .spotName(spot.getSpotName())
                        .distanceMeters(roundDouble(snappedSpot.distanceMeters(), 2))
                        .build());
            }
            snappedSpots.add(snappedSpot);
        }

        PathBuildResult pathBuildResult = buildRoutePath(snappedSpots, roadNetwork, fallbackStrategy, warnings);
        if (pathBuildResult.coordinates().size() < 2) {
            throw new BaseException("路线至少需要 2 个不同轨迹点后才能生成 GeoJSON");
        }
        double distanceMeters = calculateLineDistance(pathBuildResult.coordinates());
        JSONObject geojson = buildGeneratedGeoJson(routeId, scenicAreaId, routeName,
                request, pathBuildResult.coordinates(), distanceMeters);
        return new GeneratedRouteGeoCalculation(geojson, distanceMeters, pathBuildResult.roadSegmentCount(), warnings);
    }

    private void validateAgentRouteGeoGenerate(AgentRouteGeoGenerateDTO request) {
        if (request == null) {
            throw new BaseException("Agent定制路线参数不能为空");
        }
        if (!StringUtils.hasText(request.getScenicName())) {
            throw new BaseException("景区名称不能为空");
        }
        if (CollectionUtils.isEmpty(request.getSpotNames()) || request.getSpotNames().size() < 2) {
            throw new BaseException("定制路线至少需要 2 个有效景点");
        }
        for (String spotName : request.getSpotNames()) {
            if (!StringUtils.hasText(spotName)) {
                throw new BaseException("景点名称不能为空");
            }
        }
    }

    private ScenicArea resolveAgentScenicArea(String scenicName) {
        List<ScenicArea> matchedAreas = findMatchedScenicAreas(scenicName);
        if (matchedAreas.isEmpty()) {
            throw new BaseException("未匹配到启用景区：" + scenicName);
        }
        if (matchedAreas.size() > 1) {
            throw new BaseException("景区名称匹配到多个结果，请提供更准确的景区名称：" + scenicName);
        }
        return matchedAreas.get(0);
    }

    private List<RouteSpotDetailVO> buildAgentRouteSpots(AgentRouteGeoGenerateDTO request, ScenicArea scenicArea) {
        List<ScenicSpot> activeSpots = scenicSpotMapper.listActiveByScenicAreaId(scenicArea.getId());
        List<RouteSpotDetailVO> routeSpots = new ArrayList<>();
        for (int i = 0; i < request.getSpotNames().size(); i++) {
            String spotName = request.getSpotNames().get(i);
            ScenicSpot scenicSpot = resolveAgentScenicSpot(spotName, activeSpots);
            routeSpots.add(buildAgentRouteSpotDetail(scenicSpot, i));
        }
        return routeSpots;
    }

    private ScenicSpot resolveAgentScenicSpot(String spotName, List<ScenicSpot> activeSpots) {
        List<ScenicSpot> matchedSpots = findMatchedScenicSpots(spotName, activeSpots);
        if (matchedSpots.isEmpty()) {
            throw new BaseException("未匹配到当前景区下的启用景点：" + spotName);
        }
        if (matchedSpots.size() > 1) {
            throw new BaseException("景点名称匹配到多个结果，请提供更准确的景点名称：" + spotName);
        }
        return matchedSpots.get(0);
    }

    private List<ScenicArea> findMatchedScenicAreas(String scenicName) {
        String normalizedName = normalizeName(scenicName);
        List<ScenicArea> activeAreas = scenicAreaMapper.listAllActive();
        List<ScenicArea> exactMatches = activeAreas.stream()
                .filter(area -> normalizeName(area.getScenicName()).equals(normalizedName))
                .collect(Collectors.toList());
        if (!exactMatches.isEmpty()) {
            return exactMatches;
        }
        return activeAreas.stream()
                .filter(area -> isNameContains(area.getScenicName(), scenicName))
                .collect(Collectors.toList());
    }

    private List<ScenicSpot> findMatchedScenicSpots(String spotName, List<ScenicSpot> activeSpots) {
        String normalizedName = normalizeName(spotName);
        List<ScenicSpot> exactMatches = activeSpots.stream()
                .filter(spot -> normalizeName(spot.getSpotName()).equals(normalizedName))
                .collect(Collectors.toList());
        if (!exactMatches.isEmpty()) {
            return exactMatches;
        }
        return activeSpots.stream()
                .filter(spot -> isNameContains(spot.getSpotName(), spotName))
                .collect(Collectors.toList());
    }

    private RouteSpotDetailVO buildAgentRouteSpotDetail(ScenicSpot scenicSpot, int index) {
        return RouteSpotDetailVO.builder()
                .routeId(null)
                .spotId(scenicSpot.getId())
                .sortNo(index)
                .stayDurationMinutes(scenicSpot.getStayDurationMinutes())
                .isMustVisit(0)
                .spotName(scenicSpot.getSpotName())
                .poiType(scenicSpot.getPoiType())
                .iconType(scenicSpot.getIconType())
                .spotCode(scenicSpot.getSpotCode())
                .shortIntro(scenicSpot.getShortIntro())
                .description(scenicSpot.getDescription())
                .coverImageUrl(scenicSpot.getCoverImageUrl())
                .audioUrl(scenicSpot.getAudioUrl())
                .videoUrl(scenicSpot.getVideoUrl())
                .knowledgeDocId(scenicSpot.getKnowledgeDocId())
                .recommendedLevel(scenicSpot.getRecommendedLevel())
                .longitude(scenicSpot.getLongitude())
                .latitude(scenicSpot.getLatitude())
                .build();
    }

    private RouteGeoGenerateDTO buildRouteGeoGenerateRequest(AgentRouteGeoGenerateDTO request) {
        RouteGeoGenerateDTO routeGeoGenerateDTO = new RouteGeoGenerateDTO();
        routeGeoGenerateDTO.setRoadTypes(request.getRoadTypes());
        routeGeoGenerateDTO.setSnapToleranceMeters(request.getSnapToleranceMeters());
        routeGeoGenerateDTO.setFallbackStrategy(request.getFallbackStrategy());
        return routeGeoGenerateDTO;
    }

    private void saveAgentRouteGeo(Long userId,
                                   Long scenicAreaId,
                                   String routeName,
                                   GeneratedRouteGeoCalculation calculation,
                                   List<RouteSpotDetailVO> spots) {
        JSONArray spotIds = new JSONArray();
        JSONArray spotNames = new JSONArray();
        for (RouteSpotDetailVO spot : spots) {
            spotIds.add(spot.getSpotId());
            spotNames.add(spot.getSpotName());
        }

        LocalDateTime now = LocalDateTime.now();
        AgentRouteGeo agentRouteGeo = new AgentRouteGeo();
        agentRouteGeo.setUserId(userId);
        agentRouteGeo.setScenicAreaId(scenicAreaId);
        agentRouteGeo.setRouteName(routeName);
        agentRouteGeo.setGeojson(calculation.geojson().toJSONString());
        // 重点：按 Agent 已确认的游览顺序保存景点快照，便于后续还原用户定制路线。
        agentRouteGeo.setSpotIdsJson(spotIds.toJSONString());
        agentRouteGeo.setSpotNamesJson(spotNames.toJSONString());
        agentRouteGeo.setDistanceMeters(roundDouble(calculation.distanceMeters(), 2));
        agentRouteGeo.setSpotCount(spots.size());
        agentRouteGeo.setRoadSegmentCount(calculation.roadSegmentCount());
        agentRouteGeo.setCreateTime(now);
        agentRouteGeo.setUpdateTime(now);

        int count = agentRouteGeoMapper.insert(agentRouteGeo);
        if (count <= 0 || agentRouteGeo.getId() == null) {
            throw new BaseException("Agent定制路线保存失败");
        }
    }

    private Long requireAgentRouteUserId(AgentRouteGeoGenerateDTO request) {
        if (request.getUserId() == null) {
            throw new BaseException("用户ID不能为空");
        }
        // 重点：该接口不走 JWT，Agent 必须显式携带用户ID用于定制路线落库。
        return request.getUserId();
    }

    private void validateRouteSpotCoordinates(List<RouteSpotDetailVO> spots) {
        for (RouteSpotDetailVO spot : spots) {
            if (spot.getLongitude() == null || spot.getLatitude() == null) {
                throw new BaseException("路线中存在缺少经纬度的景点");
            }
            double lng = spot.getLongitude().doubleValue();
            double lat = spot.getLatitude().doubleValue();
            if (lng < -180 || lng > 180 || lat < -90 || lat > 90) {
                throw new BaseException("路线中存在经纬度不合法的景点");
            }
        }
    }

    private String normalizeFallbackStrategy(String fallbackStrategy) {
        if (!StringUtils.hasText(fallbackStrategy)) {
            return FALLBACK_DIRECT_SEGMENT;
        }
        String strategy = normalizeCode(fallbackStrategy);
        if (!FALLBACK_DIRECT_SEGMENT.equals(strategy) && !FALLBACK_FAIL.equals(strategy)) {
            throw new BaseException("兜底策略不支持");
        }
        return strategy;
    }

    private Set<String> normalizeRoadTypes(List<String> roadTypes) {
        if (CollectionUtils.isEmpty(roadTypes)) {
            return Collections.emptySet();
        }
        return roadTypes.stream()
                .filter(StringUtils::hasText)
                .map(this::normalizeCode)
                .collect(Collectors.toSet());
    }

    private RoadNetwork buildRoadNetwork(Long scenicAreaId, Set<String> roadTypes, List<RouteGeoGenerateWarningVO> warnings) {
        RoadNetwork roadNetwork = new RoadNetwork();
        List<ScenicGeoFeature> roadFeatures = scenicGeoFeatureMapper.listActiveByScenicAreaId(scenicAreaId)
                .stream()
                .filter(feature -> FEATURE_TYPE_ROAD.equalsIgnoreCase(feature.getFeatureType()))
                .filter(feature -> CollectionUtils.isEmpty(roadTypes)
                        || roadTypes.contains(feature.getFeatureSubType() == null ? "" : normalizeCode(feature.getFeatureSubType())))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(roadFeatures)) {
            return roadNetwork;
        }

        for (ScenicGeoFeature feature : roadFeatures) {
            List<List<GeoPoint>> lines = parseRoadGeoJson(feature, warnings);
            for (List<GeoPoint> line : lines) {
                for (int i = 0; i < line.size() - 1; i++) {
                    GeoPoint from = line.get(i);
                    GeoPoint to = line.get(i + 1);
                    if (samePoint(from, to)) {
                        continue;
                    }
                    int fromNodeId = roadNetwork.findOrCreateNode(from);
                    int toNodeId = roadNetwork.findOrCreateNode(to);
                    roadNetwork.addEdge(fromNodeId, toNodeId);
                    roadNetwork.segments.add(new RoadSegment(from, to, fromNodeId, toNodeId));
                }
            }
        }
        return roadNetwork;
    }

    private List<List<GeoPoint>> parseRoadGeoJson(ScenicGeoFeature feature, List<RouteGeoGenerateWarningVO> warnings) {
        try {
            JSONObject geoJson = JSON.parseObject(feature.getGeojson());
            List<List<GeoPoint>> lines = new ArrayList<>();
            collectLineStrings(geoJson, lines);
            if (lines.isEmpty()) {
                warnings.add(RouteGeoGenerateWarningVO.builder()
                        .code("ROAD_GEOMETRY_IGNORED")
                        .message("道路要素“" + feature.getFeatureName() + "”不是可计算的线几何，已忽略。")
                        .featureId(feature.getId())
                        .featureName(feature.getFeatureName())
                        .build());
            }
            return lines;
        } catch (JSONException | ClassCastException ex) {
            throw new BaseException("道路 GeoJSON 无法解析");
        }
    }

    private void collectLineStrings(JSONObject geoJson, List<List<GeoPoint>> lines) {
        String type = geoJson.getString("type");
        if ("Feature".equalsIgnoreCase(type)) {
            JSONObject geometry = geoJson.getJSONObject("geometry");
            if (geometry != null) {
                collectLineStrings(geometry, lines);
            }
            return;
        }
        if ("FeatureCollection".equalsIgnoreCase(type)) {
            JSONArray features = geoJson.getJSONArray("features");
            if (features == null) {
                return;
            }
            for (int i = 0; i < features.size(); i++) {
                JSONObject feature = features.getJSONObject(i);
                if (feature != null) {
                    collectLineStrings(feature, lines);
                }
            }
            return;
        }
        if ("LineString".equalsIgnoreCase(type)) {
            List<GeoPoint> line = parseLineStringCoordinates(geoJson.getJSONArray("coordinates"));
            if (line.size() >= 2) {
                lines.add(line);
            }
            return;
        }
        if ("MultiLineString".equalsIgnoreCase(type)) {
            JSONArray multiCoordinates = geoJson.getJSONArray("coordinates");
            if (multiCoordinates == null) {
                return;
            }
            for (int i = 0; i < multiCoordinates.size(); i++) {
                List<GeoPoint> line = parseLineStringCoordinates(multiCoordinates.getJSONArray(i));
                if (line.size() >= 2) {
                    lines.add(line);
                }
            }
        }
    }

    private List<GeoPoint> parseLineStringCoordinates(JSONArray coordinates) {
        List<GeoPoint> line = new ArrayList<>();
        if (coordinates == null) {
            return line;
        }
        for (int i = 0; i < coordinates.size(); i++) {
            JSONArray point = coordinates.getJSONArray(i);
            if (point == null || point.size() < 2) {
                continue;
            }
            double lng = point.getDoubleValue(0);
            double lat = point.getDoubleValue(1);
            if (lng >= -180 && lng <= 180 && lat >= -90 && lat <= 90) {
                line.add(new GeoPoint(lng, lat));
            }
        }
        return line;
    }

    private SnappedSpot snapSpotToRoad(RouteSpotDetailVO spot, RoadNetwork roadNetwork) {
        GeoPoint spotPoint = new GeoPoint(spot.getLongitude().doubleValue(), spot.getLatitude().doubleValue());
        ProjectionResult nearest = null;
        for (RoadSegment segment : roadNetwork.segments) {
            ProjectionResult projection = projectPointToSegment(spotPoint, segment);
            if (nearest == null || projection.distanceMeters() < nearest.distanceMeters()) {
                nearest = projection;
            }
        }
        if (nearest == null) {
            throw new BaseException("当前景区没有可用于计算的道路");
        }

        int snappedNodeId = roadNetwork.findOrCreateNode(nearest.projectedPoint());
        roadNetwork.addEdge(snappedNodeId, nearest.segment().fromNodeId());
        roadNetwork.addEdge(snappedNodeId, nearest.segment().toNodeId());
        return new SnappedSpot(spot, snappedNodeId, nearest.projectedPoint(), nearest.distanceMeters());
    }

    private ProjectionResult projectPointToSegment(GeoPoint point, RoadSegment segment) {
        double latRad = Math.toRadians((segment.from().lat() + segment.to().lat() + point.lat()) / 3.0);
        double scaleX = Math.cos(latRad) * 111320.0;
        double scaleY = 110540.0;
        double ax = segment.from().lng() * scaleX;
        double ay = segment.from().lat() * scaleY;
        double bx = segment.to().lng() * scaleX;
        double by = segment.to().lat() * scaleY;
        double px = point.lng() * scaleX;
        double py = point.lat() * scaleY;
        double dx = bx - ax;
        double dy = by - ay;
        double lengthSquared = dx * dx + dy * dy;
        double t = lengthSquared == 0 ? 0 : ((px - ax) * dx + (py - ay) * dy) / lengthSquared;
        t = Math.max(0, Math.min(1, t));
        GeoPoint projectedPoint = new GeoPoint(
                segment.from().lng() + (segment.to().lng() - segment.from().lng()) * t,
                segment.from().lat() + (segment.to().lat() - segment.from().lat()) * t
        );
        return new ProjectionResult(segment, projectedPoint, haversine(point, projectedPoint));
    }

    private PathBuildResult buildRoutePath(List<SnappedSpot> snappedSpots,
                                           RoadNetwork roadNetwork,
                                           String fallbackStrategy,
                                           List<RouteGeoGenerateWarningVO> warnings) {
        List<GeoPoint> coordinates = new ArrayList<>();
        int roadSegmentCount = 0;
        for (int i = 0; i < snappedSpots.size() - 1; i++) {
            SnappedSpot from = snappedSpots.get(i);
            SnappedSpot to = snappedSpots.get(i + 1);
            List<Integer> nodePath = shortestPath(roadNetwork, from.nodeId(), to.nodeId());
            List<GeoPoint> segmentCoordinates;
            if (CollectionUtils.isEmpty(nodePath)) {
                if (FALLBACK_FAIL.equals(fallbackStrategy)) {
                    throw new BaseException("相邻景点间道路不连通，且兜底策略为 FAIL");
                }
                warnings.add(RouteGeoGenerateWarningVO.builder()
                        .code("ROAD_PATH_NOT_CONNECTED")
                        .message("景点“" + from.spot().getSpotName() + "”到“" + to.spot().getSpotName() + "”之间道路不连通，已使用直线补齐。")
                        .spotId(to.spot().getSpotId())
                        .spotName(to.spot().getSpotName())
                        .build());
                segmentCoordinates = List.of(from.point(), to.point());
            } else {
                segmentCoordinates = nodePath.stream()
                        .map(nodeId -> roadNetwork.nodes.get(nodeId).point())
                        .collect(Collectors.toList());
                roadSegmentCount += Math.max(0, nodePath.size() - 1);
            }
            appendCoordinates(coordinates, segmentCoordinates);
        }
        return new PathBuildResult(coordinates, roadSegmentCount);
    }

    private List<Integer> shortestPath(RoadNetwork roadNetwork, int startNodeId, int endNodeId) {
        if (startNodeId == endNodeId) {
            return List.of(startNodeId);
        }
        Map<Integer, Double> distances = new HashMap<>();
        Map<Integer, Integer> previous = new HashMap<>();
        Set<Integer> visited = new HashSet<>();
        PriorityQueue<NodeDistance> queue = new PriorityQueue<>(Comparator.comparingDouble(item -> item.distance));
        distances.put(startNodeId, 0.0);
        queue.offer(new NodeDistance(startNodeId, 0.0));

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();
            if (!visited.add(current.nodeId())) {
                continue;
            }
            if (current.nodeId() == endNodeId) {
                break;
            }
            for (GraphEdge edge : roadNetwork.adjacency.getOrDefault(current.nodeId(), Collections.emptyList())) {
                if (visited.contains(edge.toNodeId())) {
                    continue;
                }
                double nextDistance = current.distance() + edge.weightMeters();
                if (nextDistance < distances.getOrDefault(edge.toNodeId(), Double.MAX_VALUE)) {
                    distances.put(edge.toNodeId(), nextDistance);
                    previous.put(edge.toNodeId(), current.nodeId());
                    queue.offer(new NodeDistance(edge.toNodeId(), nextDistance));
                }
            }
        }

        if (!distances.containsKey(endNodeId)) {
            return Collections.emptyList();
        }
        List<Integer> path = new ArrayList<>();
        Integer currentNodeId = endNodeId;
        while (currentNodeId != null) {
            path.add(currentNodeId);
            if (currentNodeId == startNodeId) {
                break;
            }
            currentNodeId = previous.get(currentNodeId);
        }
        Collections.reverse(path);
        return path;
    }

    private void appendCoordinates(List<GeoPoint> target, List<GeoPoint> source) {
        for (GeoPoint point : source) {
            if (target.isEmpty() || !samePoint(target.get(target.size() - 1), point)) {
                target.add(point);
            }
        }
    }

    private JSONObject buildGeneratedGeoJson(Long routeId,
                                             Long scenicAreaId,
                                             String routeName,
                                             RouteGeoGenerateDTO request,
                                             List<GeoPoint> coordinates,
                                             double distanceMeters) {
        JSONArray coordinateArray = new JSONArray();
        for (GeoPoint coordinate : coordinates) {
            JSONArray point = new JSONArray();
            point.add(roundDouble(coordinate.lng(), 6));
            point.add(roundDouble(coordinate.lat(), 6));
            coordinateArray.add(point);
        }

        JSONObject geometry = new JSONObject(true);
        geometry.put("type", "LineString");
        geometry.put("coordinates", coordinateArray);

        JSONObject properties = new JSONObject(true);
        properties.put("routeId", routeId);
        properties.put("scenicAreaId", scenicAreaId);
        properties.put("routeName", routeName);
        properties.put("generated", true);
        properties.put("algorithm", "road-network-shortest-path");
        properties.put("roadTypes", request.getRoadTypes() == null ? Collections.emptyList() : request.getRoadTypes());
        properties.put("distanceMeters", roundDouble(distanceMeters, 2));

        JSONObject feature = new JSONObject(true);
        feature.put("type", "Feature");
        feature.put("geometry", geometry);
        feature.put("properties", properties);
        return feature;
    }

    private TourRouteGeo saveGeneratedRouteGeo(Long routeId,
                                               Long scenicAreaId,
                                               JSONObject geojson,
                                               Integer version,
                                               RouteGeoGenerateDTO request) {
        Integer status = defaultStatus(request.getStatus());
        if (Boolean.TRUE.equals(request.getOverwriteActive()) && Integer.valueOf(1).equals(status)) {
            tourRouteGeoMapper.listByRouteId(routeId).stream()
                    .filter(geo -> Integer.valueOf(1).equals(geo.getStatus()))
                    .forEach(geo -> {
                        TourRouteGeo updateGeo = new TourRouteGeo();
                        updateGeo.setId(geo.getId());
                        updateGeo.setStatus(0);
                        tourRouteGeoMapper.updateById(updateGeo);
                    });
        }

        TourRouteGeo routeGeo = new TourRouteGeo();
        routeGeo.setRouteId(routeId);
        routeGeo.setScenicAreaId(scenicAreaId);
        routeGeo.setGeojson(geojson.toJSONString());
        routeGeo.setVersion(version);
        routeGeo.setStatus(status);
        routeGeo.setCreateTime(LocalDateTime.now());
        routeGeo.setUpdateTime(LocalDateTime.now());
        int count = tourRouteGeoMapper.insert(routeGeo);
        if (count <= 0 || routeGeo.getId() == null) {
            throw new BaseException("轨迹版本保存失败");
        }
        return routeGeo;
    }

    private Integer resolveGeoVersionForGenerate(Long routeId, Integer requestVersion) {
        if (requestVersion != null) {
            return requestVersion;
        }
        List<TourRouteGeo> geos = tourRouteGeoMapper.listByRouteId(routeId);
        if (CollectionUtils.isEmpty(geos)) {
            return 1;
        }
        return geos.stream()
                .map(TourRouteGeo::getVersion)
                .filter(version -> version != null)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private void updateRouteDistance(Long routeId, double distanceMeters) {
        TourRoute route = new TourRoute();
        route.setId(routeId);
        route.setDistanceMeters((int) Math.round(distanceMeters));
        tourRouteMapper.updateById(route);
    }

    private double calculateLineDistance(List<GeoPoint> coordinates) {
        double distance = 0.0;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            distance += haversine(coordinates.get(i), coordinates.get(i + 1));
        }
        return distance;
    }

    private double haversine(GeoPoint from, GeoPoint to) {
        double lat1 = Math.toRadians(from.lat());
        double lat2 = Math.toRadians(to.lat());
        double deltaLat = Math.toRadians(to.lat() - from.lat());
        double deltaLng = Math.toRadians(to.lng() - from.lng());
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    private boolean samePoint(GeoPoint from, GeoPoint to) {
        return Math.abs(from.lng() - to.lng()) < 0.0000001 && Math.abs(from.lat() - to.lat()) < 0.0000001;
    }

    private double roundDouble(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    private void deleteTourRouteCascade(Long routeId) {
        tourRouteGeoMapper.disableByRouteId(routeId);
        tourRouteMapper.logicalDeleteById(routeId);
    }

    private void replaceRouteSpots(Long routeId, Long scenicAreaId, List<TourRouteSpotDTO> routeSpots) {
        tourRouteSpotMapper.deleteByRouteId(routeId);
        if (CollectionUtils.isEmpty(routeSpots)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<TourRouteSpot> items = new ArrayList<>();
        for (int i = 0; i < routeSpots.size(); i++) {
            TourRouteSpotDTO routeSpotDTO = routeSpots.get(i);
            if (routeSpotDTO == null || routeSpotDTO.getSpotId() == null) {
                throw new BaseException("路线景点中的景点ID不能为空");
            }
            ScenicSpot scenicSpot = requireScenicSpot(routeSpotDTO.getSpotId());
            if (!scenicAreaId.equals(scenicSpot.getScenicAreaId())) {
                throw new BaseException("路线景点必须属于同一景区");
            }

            TourRouteSpot item = new TourRouteSpot();
            item.setRouteId(routeId);
            item.setSpotId(routeSpotDTO.getSpotId());
            item.setSortNo(routeSpotDTO.getSortNo() == null ? i : routeSpotDTO.getSortNo());
            item.setStayDurationMinutes(routeSpotDTO.getStayDurationMinutes());
            item.setIsMustVisit(defaultNumber(routeSpotDTO.getIsMustVisit(), 0));
            item.setRemark(routeSpotDTO.getRemark());
            item.setCreateTime(now);
            items.add(item);
        }
        tourRouteSpotMapper.batchInsert(items);
    }

    private Integer resolveGeoVersion(TourRouteGeoDTO tourRouteGeoDTO) {
        if (tourRouteGeoDTO.getVersion() != null) {
            return tourRouteGeoDTO.getVersion();
        }
        List<TourRouteGeo> geos = tourRouteGeoMapper.listByRouteId(tourRouteGeoDTO.getRouteId());
        if (CollectionUtils.isEmpty(geos)) {
            return 1;
        }
        return geos.get(0).getVersion() + 1;
    }

    private ScenicArea requireScenicArea(Long id) {
        if (id == null) {
            throw new BaseException("景区ID不能为空");
        }
        ScenicArea scenicArea = scenicAreaMapper.getById(id);
        if (scenicArea == null) {
            throw new BaseException("景区不存在");
        }
        return scenicArea;
    }

    private ScenicSpot requireScenicSpot(Long id) {
        if (id == null) {
            throw new BaseException("景点ID不能为空");
        }
        ScenicSpot scenicSpot = scenicSpotMapper.getById(id);
        if (scenicSpot == null) {
            throw new BaseException("景点不存在");
        }
        return scenicSpot;
    }

    private TourRoute requireTourRoute(Long id) {
        if (id == null) {
            throw new BaseException("路线ID不能为空");
        }
        TourRoute route = tourRouteMapper.getById(id);
        if (route == null) {
            throw new BaseException("路线不存在");
        }
        return route;
    }

    private void validateRouteGeoScenicArea(Long scenicAreaId, TourRoute route) {
        if (scenicAreaId != null && !scenicAreaId.equals(route.getScenicAreaId())) {
            throw new BaseException("路线轨迹所属景区必须和路线所属景区一致");
        }
    }

    private void validateScenicAreaForCreate(ScenicAreaDTO scenicAreaDTO) {
        if (scenicAreaDTO == null || !StringUtils.hasText(scenicAreaDTO.getScenicName())) {
            throw new BaseException("景区名称不能为空");
        }
    }

    private void validateScenicSpotForCreate(ScenicSpotDTO scenicSpotDTO) {
        if (scenicSpotDTO == null) {
            throw new BaseException("景点参数不能为空");
        }
        if (scenicSpotDTO.getScenicAreaId() == null) {
            throw new BaseException("所属景区ID不能为空");
        }
        if (!StringUtils.hasText(scenicSpotDTO.getSpotName())) {
            throw new BaseException("景点名称不能为空");
        }
    }

    private void validateRouteForCreate(TourRouteDTO tourRouteDTO) {
        if (tourRouteDTO == null) {
            throw new BaseException("路线参数不能为空");
        }
        if (tourRouteDTO.getScenicAreaId() == null) {
            throw new BaseException("所属景区ID不能为空");
        }
        if (!StringUtils.hasText(tourRouteDTO.getRouteName())) {
            throw new BaseException("路线名称不能为空");
        }
    }

    private void validateRouteGeoForCreate(TourRouteGeoDTO tourRouteGeoDTO) {
        if (tourRouteGeoDTO == null) {
            throw new BaseException("路线几何数据参数不能为空");
        }
        if (tourRouteGeoDTO.getRouteId() == null) {
            throw new BaseException("路线ID不能为空");
        }
        if (!StringUtils.hasText(tourRouteGeoDTO.getGeojson())) {
            throw new BaseException("GeoJSON不能为空");
        }
    }

    private void validateGeoFeatureForCreate(ScenicGeoFeatureDTO scenicGeoFeatureDTO) {
        if (scenicGeoFeatureDTO == null) {
            throw new BaseException("空间要素参数不能为空");
        }
        if (scenicGeoFeatureDTO.getScenicAreaId() == null) {
            throw new BaseException("景区ID不能为空");
        }
        if (!StringUtils.hasText(scenicGeoFeatureDTO.getFeatureName())) {
            throw new BaseException("要素名称不能为空");
        }
        if (!StringUtils.hasText(scenicGeoFeatureDTO.getFeatureType())) {
            throw new BaseException("要素类型不能为空");
        }
        if (!StringUtils.hasText(scenicGeoFeatureDTO.getGeojson())) {
            throw new BaseException("GeoJSON不能为空");
        }
    }

    private void normalizeAndValidateGeoFeatureForUpdate(ScenicGeoFeatureDTO scenicGeoFeatureDTO, ScenicGeoFeature existFeature) {
        ScenicGeoFeatureDTO mergedFeature = new ScenicGeoFeatureDTO();
        mergedFeature.setId(existFeature.getId());
        mergedFeature.setScenicAreaId(defaultLong(scenicGeoFeatureDTO.getScenicAreaId(), existFeature.getScenicAreaId()));
        mergedFeature.setFeatureName(defaultString(scenicGeoFeatureDTO.getFeatureName(), existFeature.getFeatureName()));
        mergedFeature.setFeatureType(defaultString(scenicGeoFeatureDTO.getFeatureType(), existFeature.getFeatureType()));
        mergedFeature.setGeometryType(defaultString(scenicGeoFeatureDTO.getGeometryType(), existFeature.getGeometryType()));
        mergedFeature.setFeatureSubType(defaultString(scenicGeoFeatureDTO.getFeatureSubType(), existFeature.getFeatureSubType()));
        mergedFeature.setLengthMeters(defaultInteger(scenicGeoFeatureDTO.getLengthMeters(), existFeature.getLengthMeters()));
        mergedFeature.setPropertiesJson(defaultString(scenicGeoFeatureDTO.getPropertiesJson(), existFeature.getPropertiesJson()));
        mergedFeature.setGeojson(defaultString(scenicGeoFeatureDTO.getGeojson(), existFeature.getGeojson()));
        normalizeAndValidateGeoFeature(mergedFeature);

        if (StringUtils.hasText(scenicGeoFeatureDTO.getFeatureType())) {
            scenicGeoFeatureDTO.setFeatureType(mergedFeature.getFeatureType());
        }
        if (StringUtils.hasText(scenicGeoFeatureDTO.getGeometryType()) || StringUtils.hasText(scenicGeoFeatureDTO.getGeojson())) {
            scenicGeoFeatureDTO.setGeometryType(mergedFeature.getGeometryType());
        }
        if (StringUtils.hasText(scenicGeoFeatureDTO.getFeatureSubType())) {
            scenicGeoFeatureDTO.setFeatureSubType(mergedFeature.getFeatureSubType());
        }
        if (scenicGeoFeatureDTO.getLengthMeters() != null) {
            scenicGeoFeatureDTO.setLengthMeters(mergedFeature.getLengthMeters());
        }
    }

    private void normalizeAndValidateGeoFeature(ScenicGeoFeatureDTO scenicGeoFeatureDTO) {
        String featureType = normalizeCode(scenicGeoFeatureDTO.getFeatureType());
        if (!SUPPORTED_FEATURE_TYPES.contains(featureType)) {
            throw new BaseException("要素类型不支持");
        }
        scenicGeoFeatureDTO.setFeatureType(featureType);

        if (StringUtils.hasText(scenicGeoFeatureDTO.getFeatureSubType())) {
            scenicGeoFeatureDTO.setFeatureSubType(normalizeCode(scenicGeoFeatureDTO.getFeatureSubType()));
        }
        if (scenicGeoFeatureDTO.getLengthMeters() != null && scenicGeoFeatureDTO.getLengthMeters() < 0) {
            throw new BaseException("要素长度不能小于0");
        }

        String geometryType = parseGeoJsonGeometryType(scenicGeoFeatureDTO.getGeojson());
        if (StringUtils.hasText(scenicGeoFeatureDTO.getGeometryType())
                && !geometryType.equals(normalizeCode(scenicGeoFeatureDTO.getGeometryType()))) {
            throw new BaseException("GeoJSON几何类型与geometryType不一致");
        }
        scenicGeoFeatureDTO.setGeometryType(geometryType);
        validateFeatureGeometryType(featureType, geometryType);

        if (StringUtils.hasText(scenicGeoFeatureDTO.getPropertiesJson())) {
            validateJsonObject(scenicGeoFeatureDTO.getPropertiesJson(), "扩展属性JSON格式不正确");
        }
    }

    private String parseGeoJsonGeometryType(String geojson) {
        JSONObject geoJsonObject = validateJsonObject(geojson, "GeoJSON格式不正确");
        JSONObject geometryObject = geoJsonObject;
        if ("Feature".equalsIgnoreCase(geoJsonObject.getString("type"))) {
            geometryObject = geoJsonObject.getJSONObject("geometry");
            if (geometryObject == null) {
                throw new BaseException("GeoJSON缺少geometry");
            }
        }

        String rawGeometryType = geometryObject.getString("type");
        if (!StringUtils.hasText(rawGeometryType)) {
            throw new BaseException("GeoJSON缺少几何类型");
        }
        if ("Point".equalsIgnoreCase(rawGeometryType) || "MultiPoint".equalsIgnoreCase(rawGeometryType)) {
            return GEOMETRY_TYPE_POINT;
        }
        if ("LineString".equalsIgnoreCase(rawGeometryType) || "MultiLineString".equalsIgnoreCase(rawGeometryType)) {
            return GEOMETRY_TYPE_LINE;
        }
        if ("Polygon".equalsIgnoreCase(rawGeometryType) || "MultiPolygon".equalsIgnoreCase(rawGeometryType)) {
            return GEOMETRY_TYPE_POLYGON;
        }
        throw new BaseException("GeoJSON几何类型不支持");
    }

    private void validateFeatureGeometryType(String featureType, String geometryType) {
        if (FEATURE_TYPE_ROAD.equals(featureType)) {
            if (!GEOMETRY_TYPE_LINE.equals(geometryType)) {
                throw new BaseException("道路要素必须使用LineString或MultiLineString");
            }
            return;
        }
        if (!GEOMETRY_TYPE_POLYGON.equals(geometryType)) {
            throw new BaseException("区域类空间要素必须使用Polygon或MultiPolygon");
        }
    }

    private JSONObject validateJsonObject(String json, String message) {
        try {
            return JSON.parseObject(json);
        } catch (JSONException | ClassCastException e) {
            throw new BaseException(message);
        }
    }

    private String normalizeCode(String value) {
        return value.trim().toUpperCase();
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isNameContains(String candidateName, String requestName) {
        String candidate = normalizeName(candidateName);
        String request = normalizeName(requestName);
        return StringUtils.hasText(candidate) && StringUtils.hasText(request)
                && (candidate.contains(request) || request.contains(candidate));
    }

    private String defaultString(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private Long defaultLong(Long value, Long defaultValue) {
        return value == null ? defaultValue : value;
    }

    private Integer defaultInteger(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private void validateInteractionLog(MapInteractionLogDTO mapInteractionLogDTO) {
        if (mapInteractionLogDTO == null) {
            throw new BaseException("交互日志参数不能为空");
        }
        if (mapInteractionLogDTO.getScenicAreaId() == null) {
            throw new BaseException("景区ID不能为空");
        }
        if (!StringUtils.hasText(mapInteractionLogDTO.getActionType())) {
            throw new BaseException("操作类型不能为空");
        }
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }

    private Integer defaultStatus(Integer status) {
        return status == null ? 1 : status;
    }

    private Integer defaultNumber(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private ScenicAreaVO buildScenicAreaVO(ScenicArea scenicArea) {
        if (scenicArea == null) {
            return null;
        }
        return ScenicAreaVO.builder()
                .id(scenicArea.getId())
                .scenicName(scenicArea.getScenicName())
                .scenicCode(scenicArea.getScenicCode())
                .province(scenicArea.getProvince())
                .city(scenicArea.getCity())
                .district(scenicArea.getDistrict())
                .address(scenicArea.getAddress())
                .longitude(scenicArea.getLongitude())
                .latitude(scenicArea.getLatitude())
                .description(scenicArea.getDescription())
                .openingHours(scenicArea.getOpeningHours())
                .contactPhone(scenicArea.getContactPhone())
                .coverImageUrl(scenicArea.getCoverImageUrl())
                .mapBaseImageUrl(scenicArea.getMapBaseImageUrl())
                .mapCenterLng(scenicArea.getMapCenterLng())
                .mapCenterLat(scenicArea.getMapCenterLat())
                .defaultZoom(scenicArea.getDefaultZoom())
                .minZoom(scenicArea.getMinZoom())
                .maxZoom(scenicArea.getMaxZoom())
                .mapBoundsJson(scenicArea.getMapBoundsJson())
                .status(scenicArea.getStatus())
                .createTime(scenicArea.getCreateTime())
                .updateTime(scenicArea.getUpdateTime())
                .build();
    }

    private ScenicSpotVO buildScenicSpotVO(ScenicSpot scenicSpot) {
        if (scenicSpot == null) {
            return null;
        }
        return ScenicSpotVO.builder()
                .id(scenicSpot.getId())
                .scenicAreaId(scenicSpot.getScenicAreaId())
                .spotName(scenicSpot.getSpotName())
                .poiType(scenicSpot.getPoiType())
                .iconType(scenicSpot.getIconType())
                .spotCode(scenicSpot.getSpotCode())
                .shortIntro(scenicSpot.getShortIntro())
                .description(scenicSpot.getDescription())
                .longitude(scenicSpot.getLongitude())
                .latitude(scenicSpot.getLatitude())
                .stayDurationMinutes(scenicSpot.getStayDurationMinutes())
                .openingHours(scenicSpot.getOpeningHours())
                .coverImageUrl(scenicSpot.getCoverImageUrl())
                .audioUrl(scenicSpot.getAudioUrl())
                .videoUrl(scenicSpot.getVideoUrl())
                .knowledgeDocId(scenicSpot.getKnowledgeDocId())
                .recommendedLevel(scenicSpot.getRecommendedLevel())
                .sortNo(scenicSpot.getSortNo())
                .status(scenicSpot.getStatus())
                .reservationEnabled(scenicSpot.getReservationEnabled())
                .reservationNotice(scenicSpot.getReservationNotice())
                .advanceReservationDays(scenicSpot.getAdvanceReservationDays())
                .minAdvanceMinutes(scenicSpot.getMinAdvanceMinutes())
                .createTime(scenicSpot.getCreateTime())
                .updateTime(scenicSpot.getUpdateTime())
                .build();
    }

    private TourRouteVO buildTourRouteVO(TourRoute tourRoute) {
        if (tourRoute == null) {
            return null;
        }
        return TourRouteVO.builder()
                .id(tourRoute.getId())
                .scenicAreaId(tourRoute.getScenicAreaId())
                .routeName(tourRoute.getRouteName())
                .routeType(tourRoute.getRouteType())
                .suitableCrowd(tourRoute.getSuitableCrowd())
                .durationMinutes(tourRoute.getDurationMinutes())
                .distanceMeters(tourRoute.getDistanceMeters())
                .description(tourRoute.getDescription())
                .recommendedReason(tourRoute.getRecommendedReason())
                .status(tourRoute.getStatus())
                .createTime(tourRoute.getCreateTime())
                .updateTime(tourRoute.getUpdateTime())
                .build();
    }

    private TourRouteGeoVO buildTourRouteGeoVO(TourRouteGeo tourRouteGeo) {
        if (tourRouteGeo == null) {
            return null;
        }
        return TourRouteGeoVO.builder()
                .id(tourRouteGeo.getId())
                .routeId(tourRouteGeo.getRouteId())
                .scenicAreaId(tourRouteGeo.getScenicAreaId())
                .geojson(tourRouteGeo.getGeojson())
                .version(tourRouteGeo.getVersion())
                .status(tourRouteGeo.getStatus())
                .createTime(tourRouteGeo.getCreateTime())
                .updateTime(tourRouteGeo.getUpdateTime())
                .build();
    }

    private AgentRouteGeoVO buildAgentRouteGeoVO(AgentRouteGeo agentRouteGeo) {
        if (agentRouteGeo == null) {
            return null;
        }
        return AgentRouteGeoVO.builder()
                .id(agentRouteGeo.getId())
                .userId(agentRouteGeo.getUserId())
                .scenicAreaId(agentRouteGeo.getScenicAreaId())
                .routeName(agentRouteGeo.getRouteName())
                .geojson(agentRouteGeo.getGeojson())
                .spotIdsJson(agentRouteGeo.getSpotIdsJson())
                .spotNamesJson(agentRouteGeo.getSpotNamesJson())
                .distanceMeters(agentRouteGeo.getDistanceMeters())
                .spotCount(agentRouteGeo.getSpotCount())
                .roadSegmentCount(agentRouteGeo.getRoadSegmentCount())
                .createTime(agentRouteGeo.getCreateTime())
                .updateTime(agentRouteGeo.getUpdateTime())
                .build();
    }

    private ScenicGeoFeatureVO buildScenicGeoFeatureVO(ScenicGeoFeature scenicGeoFeature) {
        if (scenicGeoFeature == null) {
            return null;
        }
        return ScenicGeoFeatureVO.builder()
                .id(scenicGeoFeature.getId())
                .scenicAreaId(scenicGeoFeature.getScenicAreaId())
                .featureName(scenicGeoFeature.getFeatureName())
                .featureType(scenicGeoFeature.getFeatureType())
                .geometryType(scenicGeoFeature.getGeometryType())
                .featureSubType(scenicGeoFeature.getFeatureSubType())
                .lengthMeters(scenicGeoFeature.getLengthMeters())
                .propertiesJson(scenicGeoFeature.getPropertiesJson())
                .geojson(scenicGeoFeature.getGeojson())
                .status(scenicGeoFeature.getStatus())
                .deleted(scenicGeoFeature.getDeleted())
                .createTime(scenicGeoFeature.getCreateTime())
                .updateTime(scenicGeoFeature.getUpdateTime())
                .build();
    }

    private class RoadNetwork {

        private final List<GraphNode> nodes = new ArrayList<>();

        private final Map<Integer, List<GraphEdge>> adjacency = new HashMap<>();

        private final List<RoadSegment> segments = new ArrayList<>();

        private int findOrCreateNode(GeoPoint point) {
            for (GraphNode node : nodes) {
                if (haversine(node.point(), point) <= NODE_MERGE_TOLERANCE_METERS) {
                    return node.id();
                }
            }
            int nodeId = nodes.size();
            nodes.add(new GraphNode(nodeId, point));
            return nodeId;
        }

        private void addEdge(int fromNodeId, int toNodeId) {
            if (fromNodeId == toNodeId) {
                return;
            }
            double weightMeters = haversine(nodes.get(fromNodeId).point(), nodes.get(toNodeId).point());
            adjacency.computeIfAbsent(fromNodeId, key -> new ArrayList<>())
                    .add(new GraphEdge(toNodeId, weightMeters));
            adjacency.computeIfAbsent(toNodeId, key -> new ArrayList<>())
                    .add(new GraphEdge(fromNodeId, weightMeters));
        }
    }

    private record GeoPoint(double lng, double lat) {
    }

    private record GraphNode(int id, GeoPoint point) {
    }

    private record GraphEdge(int toNodeId, double weightMeters) {
    }

    private record RoadSegment(GeoPoint from, GeoPoint to, int fromNodeId, int toNodeId) {
    }

    private record ProjectionResult(RoadSegment segment, GeoPoint projectedPoint, double distanceMeters) {
    }

    private record SnappedSpot(RouteSpotDetailVO spot, int nodeId, GeoPoint point, double distanceMeters) {
    }

    private record NodeDistance(int nodeId, double distance) {
    }

    private record PathBuildResult(List<GeoPoint> coordinates, int roadSegmentCount) {
    }

    private record GeneratedRouteGeoCalculation(JSONObject geojson,
                                                double distanceMeters,
                                                int roadSegmentCount,
                                                List<RouteGeoGenerateWarningVO> warnings) {
    }
}
