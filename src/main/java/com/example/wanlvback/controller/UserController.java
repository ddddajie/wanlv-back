package com.example.wanlvback.controller;

import com.example.wanlvback.pojo.dto.AdminCreateDTO;
import com.example.wanlvback.pojo.dto.AdminLoginDTO;
import com.example.wanlvback.pojo.dto.AdminUserUpdateDTO;
import com.example.wanlvback.pojo.dto.NormalUserLoginDTO;
import com.example.wanlvback.pojo.dto.NormalUserRegisterDTO;
import com.example.wanlvback.pojo.dto.NormalUserUpdateDTO;
import com.example.wanlvback.pojo.dto.PhoneCodeLoginDTO;
import com.example.wanlvback.pojo.dto.PhoneCodeSendDTO;
import com.example.wanlvback.pojo.dto.RealNameVerifyDTO;
import com.example.wanlvback.pojo.vo.AdminUserVO;
import com.example.wanlvback.pojo.vo.NormalUserVO;
import com.example.wanlvback.pojo.vo.PhoneCodeSendVO;
import com.example.wanlvback.pojo.vo.UserLoginVO;
import com.example.wanlvback.result.Result;
import com.example.wanlvback.service.UserService;
import com.example.wanlvback.utils.AuthUtil;
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

    /**
     * 初始化系统超级管理员账号。
     */
    @RequestMapping("/init")
    public Result<String> initSuperAdmin() {
        log.info("收到初始化超级管理员请求");
        return Result.success(userService.initSuperAdmin());
    }

    /**
     * 管理员登录。
     */
    @PostMapping("/admin/login")
    public Result<UserLoginVO> adminLogin(@RequestBody AdminLoginDTO adminLoginDTO) {
        log.info("收到管理员登录请求，username={}", adminLoginDTO.getUsername());
        return Result.success(userService.adminLogin(adminLoginDTO));
    }

    /**
     * 新增管理员账号。
     */
    @PostMapping("/admin/add")
    public Result<UserLoginVO> createAdmin(@RequestBody AdminCreateDTO adminCreateDTO) {
        AuthUtil.requireSuperAdmin();
        log.info("收到新增管理员请求，operator={}, target={}",
                adminCreateDTO.getOperatorUsername(), adminCreateDTO.getUsername());
        return Result.success(userService.createAdmin(adminCreateDTO));
    }

    /**
     * 注册普通用户账号。
     * 已停用，由手机号登陆注册代替
     */
//    @PostMapping("/normal/register")
    public Result<UserLoginVO> registerNormalUser(@RequestBody NormalUserRegisterDTO registerDTO) {
        log.info("收到普通用户注册请求，username={}", registerDTO.getUsername());
        return Result.success(userService.registerNormalUser(registerDTO));
    }

    /**
     * 普通用户登录。
     */
    @PostMapping("/normal/login")
    public Result<UserLoginVO> normalLogin(@RequestBody NormalUserLoginDTO loginDTO) {
        log.info("收到普通用户登录请求，username={}", loginDTO.getUsername());
        return Result.success(userService.normalLogin(loginDTO));
    }

    /**
     * 发送普通用户手机验证码。
     */
    @PostMapping("/normal/code/send")
    public Result<PhoneCodeSendVO> sendNormalUserPhoneCode(@RequestBody PhoneCodeSendDTO sendDTO) {
        log.info("收到普通用户手机验证码发送请求，phone={}", sendDTO == null ? null : sendDTO.getPhone());
        return Result.success(userService.sendNormalUserPhoneCode(sendDTO));
    }

    /**
     * 普通用户手机验证码登录，未注册手机号会自动创建账号。
     */
    @PostMapping("/normal/code/login")
    public Result<UserLoginVO> normalPhoneCodeLogin(@RequestBody PhoneCodeLoginDTO loginDTO) {
        log.info("收到普通用户手机验证码登录请求，phone={}", loginDTO == null ? null : loginDTO.getPhone());
        return Result.success(userService.normalPhoneCodeLogin(loginDTO));
    }

    /**
     * 普通用户实名认证。
     */
    @PostMapping("/normal/real-name/verify")
    public Result<NormalUserVO> verifyNormalUserRealName(@RequestBody RealNameVerifyDTO verifyDTO) {
        AuthUtil.requireSelf(verifyDTO == null ? null : verifyDTO.getUserId());
        log.info("收到普通用户实名认证请求，userId={}", verifyDTO == null ? null : verifyDTO.getUserId());
        return Result.success(userService.verifyNormalUserRealName(verifyDTO));
    }

    /**
     * 更新管理员账号信息。
     */
    @PutMapping("/admin/update")
    public Result<AdminUserVO> updateAdminUser(@RequestBody AdminUserUpdateDTO updateDTO) {
        AuthUtil.requireAdmin();
        log.info("收到管理员信息更新请求，id={}", updateDTO.getId());
        return Result.success(userService.updateAdminUser(updateDTO));
    }

    /**
     * 更新普通用户账号信息。
     */
    @PutMapping("/normal/update")
    public Result<NormalUserVO> updateNormalUser(@RequestBody NormalUserUpdateDTO updateDTO) {
        AuthUtil.requireSelfOrAdmin(updateDTO == null ? null : updateDTO.getId());
        log.info("收到普通用户信息更新请求，id={}", updateDTO.getId());
        return Result.success(userService.updateNormalUser(updateDTO));
    }

    /**
     * 查询管理员账号详情。
     */
    @GetMapping("/admin/{id}")
    public Result<AdminUserVO> getAdminUser(@PathVariable("id") Long id) {
        AuthUtil.requireAdmin();
        log.info("收到管理员详情查询请求，id={}", id);
        return Result.success(userService.getAdminUserById(id));
    }

    /**
     * 删除指定管理员账号。
     */
    @DeleteMapping("/admin/{id}")
    public Result<Void> deleteAdminUser(@PathVariable("id") Long id) {
        AuthUtil.requireSuperAdmin();
        log.info("收到管理员删除请求，id={}", id);
        userService.deleteAdminUser(id);
        return Result.success();
    }

    /**
     * 查询普通用户账号详情。
     */
    @GetMapping("/normal/{id}")
    public Result<NormalUserVO> getNormalUser(@PathVariable("id") Long id) {
        AuthUtil.requireSelfOrAdmin(id);
        log.info("收到普通用户详情查询请求，id={}", id);
        return Result.success(userService.getNormalUserById(id));
    }

    /**
     * 删除指定普通用户账号。
     */
    @DeleteMapping("/normal/{id}")
    public Result<Void> deleteNormalUser(@PathVariable("id") Long id) {
        AuthUtil.requireAdmin();
        log.info("收到普通用户删除请求，id={}", id);
        userService.deleteNormalUser(id);
        return Result.success();
    }

    /**
     * 分页查询管理员账号列表。
     */
    @GetMapping("/admin/page")
    public Result<PageResult> pageAdminUsers(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        AuthUtil.requireAdmin();
        log.info("收到管理员分页查询请求，pageNum={}, pageSize={}", pageNum, pageSize);
        return Result.success(userService.pageAdminUsers(pageNum, pageSize));
    }

    /**
     * 分页查询普通用户账号列表。
     */
    @GetMapping("/normal/page")
    public Result<PageResult> pageNormalUsers(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        AuthUtil.requireAdmin();
        log.info("收到普通用户分页查询请求，pageNum={}, pageSize={}", pageNum, pageSize);
        return Result.success(userService.pageNormalUsers(pageNum, pageSize));
    }
}
