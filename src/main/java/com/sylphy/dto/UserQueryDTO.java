package com.sylphy.dto;

import lombok.Data;

/**
 * 用户查询 DTO
 *
 * @author apple
 * @since 2026/1/7
 */
@Data
public class UserQueryDTO {
    /**
     * 用户类型 0:管理员 1:货主 2:调度员 3:司机
     */
    private Integer type;

    /**
     * 用户名（模糊查询）
     */
    private String username;

    /**
     * 账户状态 0:禁用 1:启用
     */
    private Integer status;

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;
}

