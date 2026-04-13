package com.example.wanlvback.service;

import com.example.wanlvback.pojo.dto.AdminCreateDTO;
import com.example.wanlvback.pojo.dto.AdminLoginDTO;
import com.example.wanlvback.pojo.dto.NormalUserLoginDTO;
import com.example.wanlvback.pojo.dto.NormalUserRegisterDTO;
import com.example.wanlvback.pojo.vo.UserLoginVO;

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
}
