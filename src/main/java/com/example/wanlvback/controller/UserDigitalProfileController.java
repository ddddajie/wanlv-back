package com.example.wanlvback.controller;

import com.example.wanlvback.pojo.vo.UserDigitalProfileVO;
import com.example.wanlvback.result.Result;
import com.example.wanlvback.service.UserDigitalProfileService;
import com.example.wanlvback.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户数字画像控制器。
 */
@RestController
@RequestMapping("/agent/digital-profile")
@Slf4j
public class UserDigitalProfileController {

    @Autowired
    private UserDigitalProfileService userDigitalProfileService;

    /**
     * 查询用户数字画像。
     */
    @GetMapping("/{userId}")
    public Result<UserDigitalProfileVO> getByUserId(@PathVariable Long userId) {
        AuthUtil.requireSelfOrAdmin(userId);
        log.info("收到用户数字画像查询请求，userId={}", userId);
        return Result.success(userDigitalProfileService.getByUserId(userId));
    }
}
