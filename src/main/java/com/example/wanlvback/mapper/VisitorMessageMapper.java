package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.VisitorMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 游客消息数据访问层
 */
public interface VisitorMessageMapper {

    /**
     * 新增消息
     * @param visitorMessage 消息实体
     * @return 影响行数
     */
    int insert(VisitorMessage visitorMessage);

    /**
     * 查询会话内最大消息序号
     * @param sessionId 会话 ID
     * @return 最大消息序号
     */
    Integer getMaxMessageNoBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 查询会话消息列表
     * @param sessionId 会话 ID
     * @return 消息列表
     */
    List<VisitorMessage> listBySessionId(@Param("sessionId") Long sessionId);
}
