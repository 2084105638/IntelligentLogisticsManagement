package com.sylphy.vo;

import lombok.Data;

/**
 * 用户信息 VO
 *
 * @author apple
 * @since 2026/1/7
 */
@Data
public class UserVO {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户类型 0:管理员 1:货主 2:调度员 3:司机
     */
    private Integer type;

    /**
     * 用户类型描述
     */
    private String typeDesc;

    /**
     * 用户名
     */
    private String username;

    /**
     * 账户状态 0:禁用 1:启用
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 邮箱（货主、司机、调度员）
     */
    private String email;

    /**
     * 手机号（货主、司机、调度员）
     */
    private String phone;

    /**
     * 角色ID（consignorId/dispatcherId/driverId）
     */
    private Long roleId;
}

