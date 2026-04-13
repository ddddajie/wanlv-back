package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.SysNormalUser;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 普通用户数据访问层
 */
public interface SysNormalUserMapper {

    /**
     * 根据账号查询普通用户。
     *
     * @param username 普通用户账号
     * @return 普通用户信息
     */
    SysNormalUser getByUsername(@Param("username") String username);

    /**
     * 根据ID查询普通用户。
     *
     * @param id 用户 ID
     * @return 普通用户信息
     */
    SysNormalUser getById(@Param("id") Long id);

    /**
     * 新增普通用户。
     *
     * @param normalUser 普通用户实体
     * @return 影响行数
     */
    int insert(SysNormalUser normalUser);

    /**
     * 更新普通用户密码。
     *
     * @param id 用户 ID
     * @param password 加密后的密码
     * @return 影响行数
     */
    int updatePasswordById(@Param("id") Long id, @Param("password") String password);

    /**
     * 更新最后登录时间。
     *
     * @param id 用户 ID
     * @param lastLoginTime 最后登录时间
     * @return 影响行数
     */
    int updateLastLoginTime(@Param("id") Long id, @Param("lastLoginTime") LocalDateTime lastLoginTime);
}
