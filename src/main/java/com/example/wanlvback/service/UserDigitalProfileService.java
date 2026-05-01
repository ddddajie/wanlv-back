package com.example.wanlvback.service;

import com.example.wanlvback.pojo.vo.UserDigitalProfileVO;

/**
 * 用户数字画像业务接口。
 */
public interface UserDigitalProfileService {

    /**
     * 查询用户数字画像。
     */
    UserDigitalProfileVO getByUserId(Long userId);

    /**
     * 基于已生成的日报刷新用户数字画像。
     */
    UserDigitalProfileVO refreshByUserId(Long userId);
}
