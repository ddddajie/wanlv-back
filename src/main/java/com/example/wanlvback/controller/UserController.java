package com.example.wanlvback.controller;

import com.example.wanlvback.pojo.dto.AdminCreateDTO;
import com.example.wanlvback.pojo.dto.AdminLoginDTO;
import com.example.wanlvback.pojo.dto.AdminUserUpdateDTO;
import com.example.wanlvback.pojo.dto.NormalUserLoginDTO;
import com.example.wanlvback.pojo.dto.NormalUserRegisterDTO;
import com.example.wanlvback.pojo.dto.NormalUserUpdateDTO;
import com.example.wanlvback.pojo.vo.AdminUserVO;
import com.example.wanlvback.pojo.vo.NormalUserVO;
import com.example.wanlvback.pojo.vo.UserLoginVO;
import com.example.wanlvback.result.Result;
import com.example.wanlvback.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.wanlvback.result.PageResult;

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

    @PutMapping("/admin/update")
    public Result<AdminUserVO> updateAdminUser(@RequestBody AdminUserUpdateDTO updateDTO) {
        log.info("收到管理员信息更新请求，id={}", updateDTO.getId());
        return Result.success(userService.updateAdminUser(updateDTO));
    }

    @PutMapping("/normal/update")
    public Result<NormalUserVO> updateNormalUser(@RequestBody NormalUserUpdateDTO updateDTO) {
        log.info("收到普通用户信息更新请求，id={}", updateDTO.getId());
        return Result.success(userService.updateNormalUser(updateDTO));
    }

    @GetMapping("/admin/{id}")
    public Result<AdminUserVO> getAdminUser(@PathVariable("id") Long id) {
        log.info("收到管理员详情查询请求，id={}", id);
        return Result.success(userService.getAdminUserById(id));
    }

    @DeleteMapping("/admin/{id}")
    public Result<Void> deleteAdminUser(@PathVariable("id") Long id) {
        log.info("delete admin user request, id={}", id);
        userService.deleteAdminUser(id);
        return Result.success();
    }

    @GetMapping("/normal/{id}")
    public Result<NormalUserVO> getNormalUser(@PathVariable("id") Long id) {
        log.info("收到普通用户详情查询请求，id={}", id);
        return Result.success(userService.getNormalUserById(id));
    }

    @DeleteMapping("/normal/{id}")
    public Result<Void> deleteNormalUser(@PathVariable("id") Long id) {
        log.info("delete normal user request, id={}", id);
        userService.deleteNormalUser(id);
        return Result.success();
    }

    @GetMapping("/admin/page")
    public Result<PageResult> pageAdminUsers(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("收到管理员分页查询请求，pageNum={}, pageSize={}", pageNum, pageSize);
        return Result.success(userService.pageAdminUsers(pageNum, pageSize));
    }

    @GetMapping("/normal/page")
    public Result<PageResult> pageNormalUsers(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("收到普通用户分页查询请求，pageNum={}, pageSize={}", pageNum, pageSize);
        return Result.success(userService.pageNormalUsers(pageNum, pageSize));
    }
}
