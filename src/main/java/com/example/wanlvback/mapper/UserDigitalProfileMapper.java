package com.example.wanlvback.mapper;

import com.example.wanlvback.pojo.entity.UserDigitalProfile;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数字画像数据访问层。
 */
public interface UserDigitalProfileMapper {

    /**
     * 查询全部用户画像，供超级管理员后台查看。
     */
    Page<UserDigitalProfile> listAll();

    /**
     * 根据用户 ID 查询画像。
     */
    UserDigitalProfile getByUserId(@Param("userId") Long userId);

    /**
     * 新增或覆盖画像。
     */
    int upsert(UserDigitalProfile userDigitalProfile);
}
