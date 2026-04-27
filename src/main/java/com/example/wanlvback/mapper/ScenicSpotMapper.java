package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.ScenicSpot;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 景点数据访问层
 */
public interface ScenicSpotMapper {

    ScenicSpot getById(@Param("id") Long id);

    ScenicSpot getActiveById(@Param("id") Long id);

    Page<ScenicSpot> pageQuery(@Param("scenicAreaId") Long scenicAreaId,
                               @Param("spotName") String spotName,
                               @Param("status") Integer status);

    List<ScenicSpot> listActiveByScenicAreaId(@Param("scenicAreaId") Long scenicAreaId);

    int insert(ScenicSpot scenicSpot);

    int updateById(ScenicSpot scenicSpot);
}
