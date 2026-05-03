package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.TourRoute;
import com.example.wanlvback.pojo.vo.MapRouteVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 路线数据访问层
 */
public interface TourRouteMapper {

    TourRoute getById(@Param("id") Long id);

    TourRoute getActiveById(@Param("id") Long id);

    Page<TourRoute> pageQuery(@Param("scenicAreaId") Long scenicAreaId,
                              @Param("routeName") String routeName,
                              @Param("status") Integer status);

    List<TourRoute> listActiveByScenicAreaId(@Param("scenicAreaId") Long scenicAreaId);

    List<MapRouteVO> listMapRoutesByScenicAreaId(@Param("scenicAreaId") Long scenicAreaId);

    int insert(TourRoute tourRoute);

    int updateById(TourRoute tourRoute);

    int logicalDeleteById(@Param("id") Long id);

    int logicalDeleteByScenicAreaId(@Param("scenicAreaId") Long scenicAreaId);
}
