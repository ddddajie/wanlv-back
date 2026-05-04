package com.example.wanlvback.constant;

/**
 * 消息提示常量类。
 */
public class MessageConstant {
    public static final String USER_ID_REQUIRED = "用户ID不能为空";
    public static final String SESSION_NOT_FOUND = "当前日期暂无可分析的会话";
    public static final String SCENIC_AREA_ID_REQUIRED = "景区ID不能为空";
    public static final String SCENIC_SESSION_NOT_FOUND = "当前日期暂无可绑定景区的会话";
    public static final String SESSION_ALREADY_ANALYZED = "当前会话已生成日报总结，已跳过";
    public static final String SESSION_ANALYZE_SUCCESS = "日报总结生成成功";
    public static final String SESSION_EMPTY = "会话不存在";
    public static final String SESSION_MESSAGE_EMPTY = "当前会话暂无消息记录";
    public static final String AGENT_ANALYSIS_EMPTY = "Agent 未返回有效分析结果";
    public static final String REQUEST_EMPTY = "请求参数不能为空";
    public static final String SUPER_ADMIN_CREDENTIAL_REQUIRED = "超级管理员账号或密码不能为空";
    public static final String SUPER_ADMIN_NOT_FOUND = "超级管理员账号不存在";
    public static final String SUPER_ADMIN_DISABLED = "超级管理员账号已被禁用";
    public static final String SUPER_ADMIN_ONLY = "只有超级管理员才能调用该接口";
    public static final String SUPER_ADMIN_AUTH_FAIL = "超级管理员账号或密码错误";
    public static final String ASK_CONTENT_REQUIRED = "提问内容不能为空";
    public static final String USER_NOT_FOUND = "用户不存在";
    public static final String USER_PROFILE_NOT_FOUND = "用户数字画像不存在";
    public static final String USER_PROFILE_SOURCE_EMPTY = "当前用户暂无可生成画像的日报数据";
}
