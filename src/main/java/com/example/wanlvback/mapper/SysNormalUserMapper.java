package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.SysNormalUser;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 普通用户数据访问层
 */
public interface SysNormalUserMapper {

    /**
     * 根据用户名查询普通用户。
     *
     * @param username 用户名
     * @return 普通用户信息
     */
    SysNormalUser getByUsername(@Param("username") String username);

    /**
     * 根据主键 ID 查询普通用户。
     *
     * @param id 用户主键 ID
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
     * 根据主键 ID 动态更新普通用户信息。
     *
     * @param normalUser 普通用户实体
     * @return 影响行数
     */
    int updateById(SysNormalUser normalUser);

    /**
     * 根据主键 ID 更新用户密码。
     *
     * @param id 用户主键 ID
     * @param password 加密后的密码
     * @return 影响行数
     */
    int updatePasswordById(@Param("id") Long id, @Param("password") String password);

    /**
     * 更新最后登录时间。
     *
     * @param id 用户主键 ID
     * @param lastLoginTime 最后登录时间
     * @return 影响行数
     */
    int updateLastLoginTime(@Param("id") Long id, @Param("lastLoginTime") LocalDateTime lastLoginTime);

    /**
     * 查询全部未删除普通用户。
     *
     * @return 普通用户列表
     */
    Page<SysNormalUser> listAll();

    Page<SysNormalUser> pageQuery();
}
