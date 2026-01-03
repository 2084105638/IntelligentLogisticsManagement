package com.sylphy.entity.dao;

import java.io.Serializable;
import lombok.Data;

/**
 * sys_consignor
 */
@Data
public class Consignor implements Serializable {
    /**
     * 货主主键 id
     */
    private Long consignorId;

    /**
     * 关联的用户 id
     */
    private Long userId;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码
     */
    private String password;

    private static final long serialVersionUID = 1L;
}