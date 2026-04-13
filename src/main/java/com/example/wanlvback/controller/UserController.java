package com.example.wanlvback.controller;

import com.example.wanlvback.pojo.dto.AdminCreateDTO;
import com.example.wanlvback.pojo.dto.AdminLoginDTO;
import com.example.wanlvback.pojo.dto.NormalUserLoginDTO;
import com.example.wanlvback.pojo.dto.NormalUserRegisterDTO;
import com.example.wanlvback.pojo.vo.UserLoginVO;
import com.example.wanlvback.result.Result;
import com.example.wanlvback.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口控制器
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/init")
    public Result<String> initSuperAdmin() {
        log.info("收到初始化超级管理员请求");
        return Result.success(userService.initSuperAdmin());
    }

    @PostMapping("/admin/login")
    public Result<UserLoginVO> adminLogin(@RequestBody AdminLoginDTO adminLoginDTO) {
        log.info("收到管理员登录请求，username={}", adminLoginDTO.getUsername());
        return Result.success(userService.adminLogin(adminLoginDTO));
    }

    @PostMapping("/admin/add")
    public Result<UserLoginVO> createAdmin(@RequestBody AdminCreateDTO adminCreateDTO) {
        log.info("收到新增管理员请求，operator={}, target={}",
                adminCreateDTO.getOperatorUsername(), adminCreateDTO.getUsername());
        return Result.success(userService.createAdmin(adminCreateDTO));
    }

    @PostMapping("/normal/register")
    public Result<UserLoginVO> registerNormalUser(@RequestBody NormalUserRegisterDTO registerDTO) {
        log.info("收到普通用户注册请求，username={}", registerDTO.getUsername());
        return Result.success(userService.registerNormalUser(registerDTO));
    }

    @PostMapping("/normal/login")
    public Result<UserLoginVO> normalLogin(@RequestBody NormalUserLoginDTO loginDTO) {
        log.info("收到普通用户登录请求，username={}", loginDTO.getUsername());
        return Result.success(userService.normalLogin(loginDTO));
    }
}
