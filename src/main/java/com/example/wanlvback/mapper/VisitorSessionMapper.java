package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.VisitorSession;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

/**
 * 游客会话数据访问层
 */
public interface VisitorSessionMapper {

    /**
     * 根据用户ID和日期查询会话
     * @param userId 用户 ID
     * @param reportDate 报告日期
     * @return 会话信息
     */
    VisitorSession getByUserIdAndReportDate(@Param("userId") Long userId, @Param("reportDate") LocalDate reportDate);

    /**
     * 根据主键查询会话
     * @param id 会话 ID
     * @return 会话信息
     */
    VisitorSession getById(@Param("id") Long id);

    /**
     * 新增会话
     * @param visitorSession 会话实体
     * @return 影响行数
     */
    int insert(VisitorSession visitorSession);

    /**
     * 更新景区绑定信息
     * @param visitorSession 会话实体
     * @return 影响行数
     */
    int updateScenicAreaInfo(VisitorSession visitorSession);

    /**
     * 更新会话分析结果
     * @param visitorSession 会话实体
     * @return 影响行数
     */
    int updateAnalysisResult(VisitorSession visitorSession);
}
