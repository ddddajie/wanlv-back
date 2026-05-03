package com.example.wanlvback.service.impl;

import com.example.wanlvback.exception.BaseException;
import com.example.wanlvback.mapper.SysAdminUserMapper;
import com.example.wanlvback.mapper.SysNormalUserMapper;
import com.example.wanlvback.pojo.dto.AdminCreateDTO;
import com.example.wanlvback.pojo.dto.AdminLoginDTO;
import com.example.wanlvback.pojo.dto.AdminUserUpdateDTO;
import com.example.wanlvback.pojo.dto.NormalUserLoginDTO;
import com.example.wanlvback.pojo.dto.NormalUserRegisterDTO;
import com.example.wanlvback.pojo.dto.NormalUserUpdateDTO;
import com.example.wanlvback.pojo.entity.SysAdminUser;
import com.example.wanlvback.pojo.entity.SysNormalUser;
import com.example.wanlvback.pojo.vo.AdminUserVO;
import com.example.wanlvback.pojo.vo.NormalUserVO;
import com.example.wanlvback.pojo.vo.UserLoginVO;
import com.example.wanlvback.result.PageResult;
import com.example.wanlvback.service.UserService;
import com.example.wanlvback.utils.PasswordUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
        if (existAdmin != null && !isDeleted(existAdmin.getDeleted())) {
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
        if (existUser != null && !isDeleted(existUser.getDeleted())) {
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminUserVO updateAdminUser(AdminUserUpdateDTO updateDTO) {
        checkUserId(updateDTO.getId(), "管理员ID不能为空");
        ensureAdminUpdateContent(updateDTO);

        SysAdminUser existAdmin = requireAdminUser(updateDTO.getId());
        checkAdminUsernameDuplicate(updateDTO.getUsername(), existAdmin.getId());

        SysAdminUser adminUser = new SysAdminUser();
        adminUser.setId(updateDTO.getId());
        adminUser.setUsername(updateDTO.getUsername());
        adminUser.setPassword(encodeIfPresent(updateDTO.getPassword()));
        adminUser.setRealName(updateDTO.getRealName());
        adminUser.setPhone(updateDTO.getPhone());
        adminUser.setEmail(updateDTO.getEmail());
        adminUser.setAvatarUrl(updateDTO.getAvatarUrl());
        adminUser.setRole(updateDTO.getRole());
        adminUser.setScenicSpot(updateDTO.getScenicSpot());
        adminUser.setStatus(updateDTO.getStatus());
        adminUser.setRemark(updateDTO.getRemark());

        sysAdminUserMapper.updateById(adminUser);
        log.info("管理员信息更新成功，id={}", updateDTO.getId());
        return buildAdminUserVO(requireAdminUser(updateDTO.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NormalUserVO updateNormalUser(NormalUserUpdateDTO updateDTO) {
        checkUserId(updateDTO.getId(), "普通用户ID不能为空");
        ensureNormalUserUpdateContent(updateDTO);

        SysNormalUser existUser = requireNormalUser(updateDTO.getId());
        checkNormalUsernameDuplicate(updateDTO.getUsername(), existUser.getId());

        SysNormalUser normalUser = new SysNormalUser();
        normalUser.setId(updateDTO.getId());
        normalUser.setUsername(updateDTO.getUsername());
        normalUser.setPassword(encodeIfPresent(updateDTO.getPassword()));
        normalUser.setNickname(updateDTO.getNickname());
        normalUser.setPhone(updateDTO.getPhone());
        normalUser.setEmail(updateDTO.getEmail());
        normalUser.setAvatarUrl(updateDTO.getAvatarUrl());
        normalUser.setGender(updateDTO.getGender());
        normalUser.setAge(updateDTO.getAge());
        normalUser.setInterestTags(updateDTO.getInterestTags());
        normalUser.setStatus(updateDTO.getStatus());

        sysNormalUserMapper.updateById(normalUser);
        log.info("普通用户信息更新成功，id={}", updateDTO.getId());
        return buildNormalUserVO(requireNormalUser(updateDTO.getId()));
    }

    @Override
    public AdminUserVO getAdminUserById(Long id) {
        checkUserId(id, "管理员ID不能为空");
        return buildAdminUserVO(requireAdminUser(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAdminUser(Long id) {
        checkUserId(id, "管理员ID不能为空");
        SysAdminUser adminUser = requireAdminUser(id);
        if ("super_admin".equals(adminUser.getRole())) {
            throw new BaseException("超级管理员不能删除");
        }
        sysAdminUserMapper.logicalDeleteById(id);
    }

    @Override
    public NormalUserVO getNormalUserById(Long id) {
        checkUserId(id, "普通用户ID不能为空");
        return buildNormalUserVO(requireNormalUser(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNormalUser(Long id) {
        checkUserId(id, "普通用户ID不能为空");
        requireNormalUser(id);
        sysNormalUserMapper.logicalDeleteById(id);
    }

    @Override
    public PageResult pageAdminUsers(Integer pageNum, Integer pageSize) {
        int validPageNum = normalizePageNum(pageNum);
        int validPageSize = normalizePageSize(pageSize);

        PageHelper.startPage(validPageNum, validPageSize);
        Page<SysAdminUser> page = sysAdminUserMapper.listAll();
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public PageResult pageNormalUsers(Integer pageNum, Integer pageSize) {
        int validPageNum = normalizePageNum(pageNum);
        int validPageSize = normalizePageSize(pageSize);

        PageHelper.startPage(validPageNum, validPageSize);
        Page<SysNormalUser> page = sysNormalUserMapper.listAll();
        return new PageResult(page.getTotal(),page.getResult());
    }

    private void checkLoginParam(String username, String password, String errorMessage) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BaseException(errorMessage);
        }
    }

    private void checkUserId(Long id, String errorMessage) {
        if (id == null) {
            throw new BaseException(errorMessage);
        }
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }

    private void ensureAdminUpdateContent(AdminUserUpdateDTO updateDTO) {
        if (!StringUtils.hasText(updateDTO.getUsername())
                && !StringUtils.hasText(updateDTO.getPassword())
                && !StringUtils.hasText(updateDTO.getRealName())
                && !StringUtils.hasText(updateDTO.getPhone())
                && !StringUtils.hasText(updateDTO.getEmail())
                && !StringUtils.hasText(updateDTO.getAvatarUrl())
                && !StringUtils.hasText(updateDTO.getRole())
                && !StringUtils.hasText(updateDTO.getScenicSpot())
                && updateDTO.getStatus() == null
                && !StringUtils.hasText(updateDTO.getRemark())) {
            throw new BaseException("管理员更新内容不能为空");
        }
    }

    private void ensureNormalUserUpdateContent(NormalUserUpdateDTO updateDTO) {
        if (!StringUtils.hasText(updateDTO.getUsername())
                && !StringUtils.hasText(updateDTO.getPassword())
                && !StringUtils.hasText(updateDTO.getNickname())
                && !StringUtils.hasText(updateDTO.getPhone())
                && !StringUtils.hasText(updateDTO.getEmail())
                && !StringUtils.hasText(updateDTO.getAvatarUrl())
                && updateDTO.getGender() == null
                && updateDTO.getAge() == null
                && !StringUtils.hasText(updateDTO.getInterestTags())
                && updateDTO.getStatus() == null) {
            throw new BaseException("普通用户更新内容不能为空");
        }
    }

    private void checkAdminUsernameDuplicate(String username, Long currentId) {
        if (!StringUtils.hasText(username)) {
            return;
        }
        SysAdminUser adminUser = sysAdminUserMapper.getByUsername(username);
        if (adminUser != null && !adminUser.getId().equals(currentId) && !isDeleted(adminUser.getDeleted())) {
            throw new BaseException("管理员账号已存在");
        }
    }

    private void checkNormalUsernameDuplicate(String username, Long currentId) {
        if (!StringUtils.hasText(username)) {
            return;
        }
        SysNormalUser normalUser = sysNormalUserMapper.getByUsername(username);
        if (normalUser != null && !normalUser.getId().equals(currentId) && !isDeleted(normalUser.getDeleted())) {
            throw new BaseException("普通用户账号已存在");
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

    private String encodeIfPresent(String password) {
        return StringUtils.hasText(password) ? PasswordUtil.encode(password) : null;
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

    private SysAdminUser requireAdminUser(Long id) {
        SysAdminUser adminUser = sysAdminUserMapper.getById(id);
        if (adminUser == null || isDeleted(adminUser.getDeleted())) {
            throw new BaseException("管理员用户不存在");
        }
        return adminUser;
    }

    private SysNormalUser requireNormalUser(Long id) {
        SysNormalUser normalUser = sysNormalUserMapper.getById(id);
        if (normalUser == null || isDeleted(normalUser.getDeleted())) {
            throw new BaseException("普通用户不存在");
        }
        return normalUser;
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

    private AdminUserVO buildAdminUserVO(SysAdminUser adminUser) {
        return AdminUserVO.builder()
                .id(adminUser.getId())
                .username(adminUser.getUsername())
                .realName(adminUser.getRealName())
                .phone(adminUser.getPhone())
                .email(adminUser.getEmail())
                .avatarUrl(adminUser.getAvatarUrl())
                .role(adminUser.getRole())
                .scenicSpot(adminUser.getScenicSpot())
                .status(adminUser.getStatus())
                .lastLoginTime(adminUser.getLastLoginTime())
                .remark(adminUser.getRemark())
                .createTime(adminUser.getCreateTime())
                .updateTime(adminUser.getUpdateTime())
                .build();
    }

    private NormalUserVO buildNormalUserVO(SysNormalUser normalUser) {
        return NormalUserVO.builder()
                .id(normalUser.getId())
                .username(normalUser.getUsername())
                .nickname(normalUser.getNickname())
                .phone(normalUser.getPhone())
                .email(normalUser.getEmail())
                .avatarUrl(normalUser.getAvatarUrl())
                .gender(normalUser.getGender())
                .age(normalUser.getAge())
                .interestTags(normalUser.getInterestTags())
                .status(normalUser.getStatus())
                .lastLoginTime(normalUser.getLastLoginTime())
                .createTime(normalUser.getCreateTime())
                .updateTime(normalUser.getUpdateTime())
                .build();
    }
}
