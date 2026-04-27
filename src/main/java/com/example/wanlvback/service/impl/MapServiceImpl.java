package com.example.wanlvback.service.impl;

import com.example.wanlvback.exception.BaseException;
import com.example.wanlvback.mapper.MapInteractionLogMapper;
import com.example.wanlvback.mapper.ScenicAreaMapper;
import com.example.wanlvback.mapper.ScenicGeoFeatureMapper;
import com.example.wanlvback.mapper.ScenicSpotMapper;
import com.example.wanlvback.mapper.TourRouteGeoMapper;
import com.example.wanlvback.mapper.TourRouteMapper;
import com.example.wanlvback.mapper.TourRouteSpotMapper;
import com.example.wanlvback.pojo.dto.MapInteractionLogDTO;
import com.example.wanlvback.pojo.dto.ScenicAreaDTO;
import com.example.wanlvback.pojo.dto.ScenicGeoFeatureDTO;
import com.example.wanlvback.pojo.dto.ScenicSpotDTO;
import com.example.wanlvback.pojo.dto.TourRouteDTO;
import com.example.wanlvback.pojo.dto.TourRouteGeoDTO;
import com.example.wanlvback.pojo.dto.TourRouteSpotDTO;
import com.example.wanlvback.pojo.entity.MapInteractionLog;
import com.example.wanlvback.pojo.entity.ScenicArea;
import com.example.wanlvback.pojo.entity.ScenicGeoFeature;
import com.example.wanlvback.pojo.entity.ScenicSpot;
import com.example.wanlvback.pojo.entity.TourRoute;
import com.example.wanlvback.pojo.entity.TourRouteGeo;
import com.example.wanlvback.pojo.entity.TourRouteSpot;
import com.example.wanlvback.pojo.vo.MapInitVO;
import com.example.wanlvback.pojo.vo.MapRouteVO;
import com.example.wanlvback.pojo.vo.RouteDetailVO;
import com.example.wanlvback.pojo.vo.RouteSpotDetailVO;
import com.example.wanlvback.pojo.vo.ScenicAreaVO;
import com.example.wanlvback.pojo.vo.ScenicGeoFeatureVO;
import com.example.wanlvback.pojo.vo.ScenicSpotVO;
import com.example.wanlvback.pojo.vo.TourRouteGeoVO;
import com.example.wanlvback.pojo.vo.TourRouteVO;
import com.example.wanlvback.result.PageResult;
import com.example.wanlvback.service.MapService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 地图业务服务实现
 */
@Service
@Slf4j
public class MapServiceImpl implements MapService {

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
        List<RouteSpotDetailVO> spots = tourRouteSpotMapper.listDetailByRouteId(id);
        return RouteDetailVO.builder()
                .route(buildTourRouteVO(route))
                .routeGeo(buildTourRouteGeoVO(routeGeo))
                .spots(spots)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TourRouteGeoVO createTourRouteGeo(TourRouteGeoDTO tourRouteGeoDTO) {
        validateRouteGeoForCreate(tourRouteGeoDTO);
        requireTourRoute(tourRouteGeoDTO.getRouteId());

        LocalDateTime now = LocalDateTime.now();
        TourRouteGeo tourRouteGeo = new TourRouteGeo();
        BeanUtils.copyProperties(tourRouteGeoDTO, tourRouteGeo);
        tourRouteGeo.setVersion(resolveGeoVersion(tourRouteGeoDTO));
        tourRouteGeo.setStatus(defaultStatus(tourRouteGeoDTO.getStatus()));
        tourRouteGeo.setCreateTime(now);
        tourRouteGeo.setUpdateTime(now);
        tourRouteGeoMapper.insert(tourRouteGeo);
        return buildTourRouteGeoVO(tourRouteGeoMapper.getById(tourRouteGeo.getId()));
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
        if (tourRouteGeoDTO.getRouteId() != null) {
            requireTourRoute(tourRouteGeoDTO.getRouteId());
        }

        TourRouteGeo tourRouteGeo = new TourRouteGeo();
        BeanUtils.copyProperties(tourRouteGeoDTO, tourRouteGeo);
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
            throw new BaseException("区域名称不能为空");
        }
        if (!StringUtils.hasText(scenicGeoFeatureDTO.getFeatureType())) {
            throw new BaseException("要素类型不能为空");
        }
        if (!StringUtils.hasText(scenicGeoFeatureDTO.getGeojson())) {
            throw new BaseException("GeoJSON不能为空");
        }
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
                .geojson(tourRouteGeo.getGeojson())
                .version(tourRouteGeo.getVersion())
                .status(tourRouteGeo.getStatus())
                .createTime(tourRouteGeo.getCreateTime())
                .updateTime(tourRouteGeo.getUpdateTime())
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
                .geojson(scenicGeoFeature.getGeojson())
                .status(scenicGeoFeature.getStatus())
                .deleted(scenicGeoFeature.getDeleted())
                .createTime(scenicGeoFeature.getCreateTime())
                .updateTime(scenicGeoFeature.getUpdateTime())
                .build();
    }
}
