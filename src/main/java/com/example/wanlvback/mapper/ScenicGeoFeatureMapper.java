package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.ScenicGeoFeature;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 景区空间要素数据访问层
 */
public interface ScenicGeoFeatureMapper {

    ScenicGeoFeature getById(@Param("id") Long id);

    List<ScenicGeoFeature> listByScenicAreaId(@Param("scenicAreaId") Long scenicAreaId);

    List<ScenicGeoFeature> listActiveByScenicAreaId(@Param("scenicAreaId") Long scenicAreaId);

    int insert(ScenicGeoFeature scenicGeoFeature);

    int updateById(ScenicGeoFeature scenicGeoFeature);

    int logicalDeleteById(@Param("id") Long id);

    int logicalDeleteByScenicAreaId(@Param("scenicAreaId") Long scenicAreaId);
}
