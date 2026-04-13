package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.SysAdminUser;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

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
     * 新增管理员。
     *
     * @param adminUser 管理员实体
     * @return 影响行数
     */
    int insert(SysAdminUser adminUser);

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
}
