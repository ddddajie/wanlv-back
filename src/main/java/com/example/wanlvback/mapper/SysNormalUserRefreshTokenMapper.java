package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.SysNormalUserRefreshToken;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 普通用户 refreshToken 数据访问层。
 */
public interface SysNormalUserRefreshTokenMapper {

    SysNormalUserRefreshToken getByTokenHash(@Param("tokenHash") String tokenHash);

    int insert(SysNormalUserRefreshToken refreshToken);

    int invalidateIfAvailable(@Param("tokenHash") String tokenHash,
                              @Param("now") LocalDateTime now);

    int invalidate(@Param("tokenHash") String tokenHash);
}
