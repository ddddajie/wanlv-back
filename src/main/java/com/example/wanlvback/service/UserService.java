package com.example.wanlvback.service;

import com.example.wanlvback.pojo.dto.AdminCreateDTO;
import com.example.wanlvback.pojo.dto.AdminLoginDTO;
import com.example.wanlvback.pojo.dto.AdminUserUpdateDTO;
import com.example.wanlvback.pojo.dto.NormalUserLoginDTO;
import com.example.wanlvback.pojo.dto.NormalUserRegisterDTO;
import com.example.wanlvback.pojo.dto.NormalUserUpdateDTO;
import com.example.wanlvback.pojo.vo.AdminUserVO;
import com.example.wanlvback.pojo.vo.NormalUserVO;
import com.example.wanlvback.pojo.vo.UserLoginVO;
import com.example.wanlvback.result.PageResult;

/**
 * 用户业务层
 */
public interface UserService {

    /**
     * 初始化超级管理员。
     *
     * @return 初始化结果
     */
    String initSuperAdmin();

    /**
     * 管理员登录。
     *
     * @param adminLoginDTO 登录参数
     * @return 登录结果
     */
    UserLoginVO adminLogin(AdminLoginDTO adminLoginDTO);

    /**
     * 新增管理员。
     *
     * @param adminCreateDTO 新增参数
     * @return 新增结果
     */
    UserLoginVO createAdmin(AdminCreateDTO adminCreateDTO);

    /**
     * 普通用户注册。
     *
     * @param registerDTO 注册参数
     * @return 注册结果
     */
    UserLoginVO registerNormalUser(NormalUserRegisterDTO registerDTO);

    /**
     * 普通用户登录。
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    UserLoginVO normalLogin(NormalUserLoginDTO loginDTO);

    /**
     * 更新管理员信息。
     *
     * @param updateDTO 更新参数
     * @return 管理员详情
     */
    AdminUserVO updateAdminUser(AdminUserUpdateDTO updateDTO);

    /**
     * 更新普通用户信息。
     *
     * @param updateDTO 更新参数
     * @return 普通用户详情
     */
    NormalUserVO updateNormalUser(NormalUserUpdateDTO updateDTO);

    /**
     * 查询管理员详情。
     *
     * @param id 管理员 ID
     * @return 管理员详情
     */
    AdminUserVO getAdminUserById(Long id);

    /**
     * 查询普通用户详情。
     *
     * @param id 普通用户 ID
     * @return 普通用户详情
     */
    NormalUserVO getNormalUserById(Long id);

    /**
     * 分页查询管理员列表。
     *
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult pageAdminUsers(Integer pageNum, Integer pageSize);

    /**
     * 分页查询普通用户列表。
     *
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult pageNormalUsers(Integer pageNum, Integer pageSize);
}
