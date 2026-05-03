package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.SysAdminUser;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员用户数据访问层
 */
public interface SysAdminUserMapper {

    /**
     * 根据账号查询管理员。
     *
     * @param username 管理员账号
     * @return 管理员信息
     */
    SysAdminUser getByUsername(@Param("username") String username);

    /**
     * 根据主键 ID 查询管理员。
     *
     * @param id 管理员 ID
     * @return 管理员信息
     */
    SysAdminUser getById(@Param("id") Long id);

    /**
     * 新增管理员。
     *
     * @param adminUser 管理员实体
     * @return 影响行数
     */
    int insert(SysAdminUser adminUser);

    /**
     * 根据主键 ID 动态更新管理员信息。
     *
     * @param adminUser 管理员实体
     * @return 影响行数
     */
    int updateById(SysAdminUser adminUser);

    int logicalDeleteById(@Param("id") Long id);

    /**
     * 更新管理员密码。
     *
     * @param id 管理员 ID
     * @param password 加密后的密码
     * @return 影响行数
     */
    int updatePasswordById(@Param("id") Long id, @Param("password") String password);

    /**
     * 更新最后登录时间。
     *
     * @param id 管理员 ID
     * @param lastLoginTime 最后登录时间
     * @return 影响行数
     */
    int updateLastLoginTime(@Param("id") Long id, @Param("lastLoginTime") LocalDateTime lastLoginTime);

    /**
     * 查询全部未删除管理员。
     *
     * @return 管理员列表
     */
    Page<SysAdminUser> listAll();
}
