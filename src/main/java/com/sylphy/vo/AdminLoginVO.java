package com.sylphy.vo;

import lombok.Data;

/**
 * 管理员登录响应 VO
 *
 * @author apple
 * @since 2026/1/7
 */
@Data
public class AdminLoginVO {
    private String token;
    private Long userId;
    private String username;
}

