package com.example.wanlvback.service.impl;

import com.example.wanlvback.exception.BaseException;
import com.example.wanlvback.mapper.SysAdminUserMapper;
import com.example.wanlvback.mapper.SysNormalUserMapper;
import com.example.wanlvback.pojo.dto.AdminCreateDTO;
import com.example.wanlvback.pojo.dto.AdminLoginDTO;
import com.example.wanlvback.pojo.dto.NormalUserLoginDTO;
import com.example.wanlvback.pojo.dto.NormalUserRegisterDTO;
import com.example.wanlvback.pojo.entity.SysAdminUser;
import com.example.wanlvback.pojo.entity.SysNormalUser;
import com.example.wanlvback.pojo.vo.UserLoginVO;
import com.example.wanlvback.service.UserService;
import com.example.wanlvback.utils.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户业务实现类
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private SysAdminUserMapper sysAdminUserMapper;

    @Autowired
    private SysNormalUserMapper sysNormalUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String initSuperAdmin() {
        log.info("开始初始化超级管理员");
        SysAdminUser existAdmin = sysAdminUserMapper.getByUsername("admin");
        if (existAdmin != null) {
            log.info("超级管理员已存在，username={}", existAdmin.getUsername());
            return "超级管理员已存在";
        }

        LocalDateTime now = LocalDateTime.now();
        SysAdminUser adminUser = new SysAdminUser();
        adminUser.setUsername("admin");
        adminUser.setPassword(PasswordUtil.encode("123456"));
        adminUser.setRealName("系统超级管理员");
        adminUser.setPhone("13800000000");
        adminUser.setEmail("admin@wanlv.com");
        adminUser.setAvatarUrl("https://example.com/admin.png");
        adminUser.setRole("super_admin");
        adminUser.setScenicSpot("默认景区");
        adminUser.setStatus(1);
        adminUser.setDeleted(0);
        adminUser.setRemark("由初始化接口自动生成");
        adminUser.setCreateTime(now);
        adminUser.setUpdateTime(now);
        sysAdminUserMapper.insert(adminUser);
        log.info("初始化超级管理员成功，username={}", adminUser.getUsername());
        return "超级管理员初始化成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginVO adminLogin(AdminLoginDTO adminLoginDTO) {
        checkLoginParam(adminLoginDTO.getUsername(), adminLoginDTO.getPassword(), "管理员账号或密码不能为空");
        log.info("管理员发起登录，username={}", adminLoginDTO.getUsername());

        SysAdminUser adminUser = sysAdminUserMapper.getByUsername(adminLoginDTO.getUsername());
        if (adminUser == null || isDeleted(adminUser.getDeleted())) {
            log.info("管理员登录失败，账号不存在，username={}", adminLoginDTO.getUsername());
            throw new BaseException("管理员账号不存在");
        }
        if (isDisabled(adminUser.getStatus())) {
            log.info("管理员登录失败，账号被禁用，username={}", adminLoginDTO.getUsername());
            throw new BaseException("管理员账号已被禁用");
        }

        verifyAdminPassword(adminUser, adminLoginDTO.getPassword(), "管理员账号或密码错误");

        LocalDateTime now = LocalDateTime.now();
        sysAdminUserMapper.updateLastLoginTime(adminUser.getId(), now);
        adminUser.setLastLoginTime(now);
        log.info("管理员登录成功，username={}, role={}", adminUser.getUsername(), adminUser.getRole());
        return buildAdminLoginVO(adminUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginVO createAdmin(AdminCreateDTO adminCreateDTO) {
        checkLoginParam(adminCreateDTO.getOperatorUsername(), adminCreateDTO.getOperatorPassword(), "操作人账号或密码不能为空");
        checkLoginParam(adminCreateDTO.getUsername(), adminCreateDTO.getPassword(), "新管理员账号或密码不能为空");
        log.info("收到新增管理员请求，operator={}, target={}",
                adminCreateDTO.getOperatorUsername(), adminCreateDTO.getUsername());

        SysAdminUser operator = sysAdminUserMapper.getByUsername(adminCreateDTO.getOperatorUsername());
        if (operator == null || isDeleted(operator.getDeleted())) {
            log.info("新增管理员失败，操作人不存在，operator={}", adminCreateDTO.getOperatorUsername());
            throw new BaseException("操作人不存在");
        }
        if (isDisabled(operator.getStatus())) {
            log.info("新增管理员失败，操作人已被禁用，operator={}", adminCreateDTO.getOperatorUsername());
            throw new BaseException("操作人已被禁用");
        }

        verifyAdminPassword(operator, adminCreateDTO.getOperatorPassword(), "操作人账号或密码错误");

        if (!"super_admin".equals(operator.getRole())) {
            log.info("新增管理员失败，操作人不是超级管理员，operator={}", adminCreateDTO.getOperatorUsername());
            throw new BaseException("只有超级管理员才能新增管理员");
        }

        SysAdminUser existAdmin = sysAdminUserMapper.getByUsername(adminCreateDTO.getUsername());
        if (existAdmin != null) {
            log.info("新增管理员失败，目标账号已存在，target={}", adminCreateDTO.getUsername());
            throw new BaseException("管理员账号已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        SysAdminUser adminUser = new SysAdminUser();
        adminUser.setUsername(adminCreateDTO.getUsername());
        adminUser.setPassword(PasswordUtil.encode(adminCreateDTO.getPassword()));
        adminUser.setRealName(defaultIfBlank(adminCreateDTO.getRealName(), "普通管理员"));
        adminUser.setPhone(adminCreateDTO.getPhone());
        adminUser.setEmail(adminCreateDTO.getEmail());
        adminUser.setAvatarUrl(adminCreateDTO.getAvatarUrl());
        adminUser.setRole("admin");
        adminUser.setScenicSpot(adminCreateDTO.getScenicSpot());
        adminUser.setStatus(1);
        adminUser.setDeleted(0);
        adminUser.setRemark(adminCreateDTO.getRemark());
        adminUser.setCreateTime(now);
        adminUser.setUpdateTime(now);
        sysAdminUserMapper.insert(adminUser);
        log.info("新增管理员成功，target={}", adminUser.getUsername());
        return buildAdminLoginVO(adminUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginVO registerNormalUser(NormalUserRegisterDTO registerDTO) {
        checkLoginParam(registerDTO.getUsername(), registerDTO.getPassword(), "普通用户账号或密码不能为空");
        log.info("普通用户发起注册，username={}", registerDTO.getUsername());

        SysNormalUser existUser = sysNormalUserMapper.getByUsername(registerDTO.getUsername());
        if (existUser != null) {
            log.info("普通用户注册失败，账号已存在，username={}", registerDTO.getUsername());
            throw new BaseException("普通用户账号已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        SysNormalUser normalUser = new SysNormalUser();
        normalUser.setUsername(registerDTO.getUsername());
        normalUser.setPassword(PasswordUtil.encode(registerDTO.getPassword()));
        normalUser.setNickname(defaultIfBlank(registerDTO.getNickname(), registerDTO.getUsername()));
        normalUser.setPhone(registerDTO.getPhone());
        normalUser.setEmail(registerDTO.getEmail());
        normalUser.setAvatarUrl(registerDTO.getAvatarUrl());
        normalUser.setGender(registerDTO.getGender());
        normalUser.setAge(registerDTO.getAge());
        normalUser.setInterestTags(registerDTO.getInterestTags());
        normalUser.setStatus(1);
        normalUser.setDeleted(0);
        normalUser.setLastLoginTime(now);
        normalUser.setCreateTime(now);
        normalUser.setUpdateTime(now);
        sysNormalUserMapper.insert(normalUser);
        log.info("普通用户注册成功，username={}", normalUser.getUsername());
        return buildNormalLoginVO(normalUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginVO normalLogin(NormalUserLoginDTO loginDTO) {
        checkLoginParam(loginDTO.getUsername(), loginDTO.getPassword(), "普通用户账号或密码不能为空");
        log.info("普通用户发起登录，username={}", loginDTO.getUsername());

        SysNormalUser normalUser = sysNormalUserMapper.getByUsername(loginDTO.getUsername());
        if (normalUser == null || isDeleted(normalUser.getDeleted())) {
            log.info("普通用户登录失败，账号不存在，username={}", loginDTO.getUsername());
            throw new BaseException("普通用户账号不存在");
        }
        if (isDisabled(normalUser.getStatus())) {
            log.info("普通用户登录失败，账号被禁用，username={}", loginDTO.getUsername());
            throw new BaseException("普通用户账号已被禁用");
        }

        verifyNormalUserPassword(normalUser, loginDTO.getPassword(), "普通用户账号或密码错误");

        LocalDateTime now = LocalDateTime.now();
        sysNormalUserMapper.updateLastLoginTime(normalUser.getId(), now);
        normalUser.setLastLoginTime(now);
        log.info("普通用户登录成功，username={}", normalUser.getUsername());
        return buildNormalLoginVO(normalUser);
    }

    private void checkLoginParam(String username, String password, String errorMessage) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BaseException(errorMessage);
        }
    }

    private void verifyAdminPassword(SysAdminUser adminUser, String rawPassword, String errorMessage) {
        if (PasswordUtil.isEncoded(adminUser.getPassword())) {
            if (!PasswordUtil.matches(rawPassword, adminUser.getPassword())) {
                log.info("管理员密码校验失败，username={}", adminUser.getUsername());
                throw new BaseException(errorMessage);
            }
            return;
        }

        if (!rawPassword.equals(adminUser.getPassword())) {
            log.info("管理员密码校验失败，username={}", adminUser.getUsername());
            throw new BaseException(errorMessage);
        }

        upgradeAdminPassword(adminUser, rawPassword);
    }

    private void verifyNormalUserPassword(SysNormalUser normalUser, String rawPassword, String errorMessage) {
        if (PasswordUtil.isEncoded(normalUser.getPassword())) {
            if (!PasswordUtil.matches(rawPassword, normalUser.getPassword())) {
                log.info("普通用户密码校验失败，username={}", normalUser.getUsername());
                throw new BaseException(errorMessage);
            }
            return;
        }

        if (!rawPassword.equals(normalUser.getPassword())) {
            log.info("普通用户密码校验失败，username={}", normalUser.getUsername());
            throw new BaseException(errorMessage);
        }

        upgradeNormalUserPassword(normalUser, rawPassword);
    }

    private void upgradeAdminPassword(SysAdminUser adminUser, String rawPassword) {
        String encodedPassword = PasswordUtil.encode(rawPassword);
        sysAdminUserMapper.updatePasswordById(adminUser.getId(), encodedPassword);
        adminUser.setPassword(encodedPassword);
        log.info("管理员旧版明文密码已升级，username={}", adminUser.getUsername());
    }

    private void upgradeNormalUserPassword(SysNormalUser normalUser, String rawPassword) {
        String encodedPassword = PasswordUtil.encode(rawPassword);
        sysNormalUserMapper.updatePasswordById(normalUser.getId(), encodedPassword);
        normalUser.setPassword(encodedPassword);
        log.info("普通用户旧版明文密码已升级，username={}", normalUser.getUsername());
    }

    private boolean isDeleted(Integer deleted) {
        return deleted != null && deleted == 1;
    }

    private boolean isDisabled(Integer status) {
        return status != null && status == 0;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private UserLoginVO buildAdminLoginVO(SysAdminUser adminUser) {
        return UserLoginVO.builder()
                .id(adminUser.getId())
                .username(adminUser.getUsername())
                .displayName(defaultIfBlank(adminUser.getRealName(), adminUser.getUsername()))
                .userType("admin")
                .role(adminUser.getRole())
                .status(adminUser.getStatus())
                .lastLoginTime(adminUser.getLastLoginTime())
                .build();
    }

    private UserLoginVO buildNormalLoginVO(SysNormalUser normalUser) {
        return UserLoginVO.builder()
                .id(normalUser.getId())
                .username(normalUser.getUsername())
                .displayName(defaultIfBlank(normalUser.getNickname(), normalUser.getUsername()))
                .userType("normal")
                .role("normal_user")
                .status(normalUser.getStatus())
                .lastLoginTime(normalUser.getLastLoginTime())
                .build();
    }
}
