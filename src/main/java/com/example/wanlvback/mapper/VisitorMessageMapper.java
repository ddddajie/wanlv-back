package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.VisitorMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会话消息数据访问层。
 */
public interface VisitorMessageMapper {

    /**
     * 插入一条会话消息。
     *
     * @param visitorMessage 消息实体
     * @return 影响行数
     */
    int insert(VisitorMessage visitorMessage);

    /**
     * 查询某个会话当前最大的消息序号。
     *
     * @param sessionId 会话主键 ID
     * @return 当前最大消息序号
     */
    Integer getMaxMessageNoBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 按会话主键查询消息列表。
     *
     * @param sessionId 会话主键 ID
     * @return 消息列表
     */
    List<VisitorMessage> listBySessionId(@Param("sessionId") Long sessionId);
}
