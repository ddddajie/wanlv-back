package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.ScenicArea;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 景区数据访问层
 */
public interface ScenicAreaMapper {

    ScenicArea getById(@Param("id") Long id);

    ScenicArea getActiveById(@Param("id") Long id);

    Page<ScenicArea> pageQuery(@Param("scenicName") String scenicName, @Param("status") Integer status);

    List<ScenicArea> listAllActive();

    int insert(ScenicArea scenicArea);

    int updateById(ScenicArea scenicArea);
}
