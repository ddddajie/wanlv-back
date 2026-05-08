package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.AgentRouteGeo;
import org.apache.ibatis.annotations.Param;

/**
 * Agent 用户定制路线几何数据访问层
 */
public interface AgentRouteGeoMapper {

    AgentRouteGeo getById(@Param("id") Long id);

    AgentRouteGeo getLatestByUserIdAndScenicAreaId(@Param("userId") Long userId,
                                                   @Param("scenicAreaId") Long scenicAreaId);

    int insert(AgentRouteGeo agentRouteGeo);
}
