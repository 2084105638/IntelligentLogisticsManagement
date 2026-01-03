package com.sylphy.entity.dao;

import java.io.Serializable;
import lombok.Data;

/**
 * sys_user
 */
@Data
public class User implements Serializable {
    /**
     * 用户主键id
     */
    private Long userId;

    /**
     * 用户类型 0:管理员 1:货主 2:调度员 3:司机
     */
    private Boolean type;

    private static final long serialVersionUID = 1L;
}