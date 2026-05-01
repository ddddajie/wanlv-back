package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.VisitorSession;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 会话数据访问层。
 */
public interface VisitorSessionMapper {

    /**
     * 根据用户 ID 和业务日期查询当天会话。
     */
    VisitorSession getByUserIdAndReportDate(@Param("userId") Long userId, @Param("reportDate") LocalDate reportDate);

    /**
     * 根据主键 ID 查询会话。
     */
    VisitorSession getById(@Param("id") Long id);

    /**
     * 查询某一天的全部会话，用于跑日报定时任务。
     */
    List<VisitorSession> listByReportDate(@Param("reportDate") LocalDate reportDate);

    /**
     * 查询用户已完成日报分析的会话，用于生成数字画像。
     */
    List<VisitorSession> listAnalyzedByUserId(@Param("userId") Long userId);

    /**
     * 新增一条会话记录。
     */
    int insert(VisitorSession visitorSession);

    /**
     * 更新会话中的景区绑定信息。
     */
    int updateScenicAreaInfo(VisitorSession visitorSession);

    /**
     * 更新会话分析结果。
     */
    int updateAnalysisResult(VisitorSession visitorSession);
}
