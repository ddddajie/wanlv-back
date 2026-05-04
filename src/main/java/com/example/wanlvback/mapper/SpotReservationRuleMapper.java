package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.SpotReservationRule;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SpotReservationRuleMapper {

    SpotReservationRule getById(@Param("id") Long id);

    Page<SpotReservationRule> pageQuery(@Param("scenicAreaId") Long scenicAreaId,
                                        @Param("spotId") Long spotId,
                                        @Param("status") Integer status);

    List<SpotReservationRule> listEnabledForGenerate(@Param("scenicAreaId") Long scenicAreaId,
                                                     @Param("spotId") Long spotId);

    int insert(SpotReservationRule rule);

    int updateById(SpotReservationRule rule);

    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("updateBy") Long updateBy);
}
