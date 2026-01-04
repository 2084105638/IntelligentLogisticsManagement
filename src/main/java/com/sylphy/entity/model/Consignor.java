package com.sylphy.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * sys_consignor
 */
@Data
@TableName("sys_consignor")
public class Consignor implements Serializable {
    /**
     * 货主主键 id
     */
    @TableId(value = "consignor_id", type = IdType.ASSIGN_ID)
    private Long consignorId;

    /**
     * 关联的用户 id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    private static final long serialVersionUID = 1L;
}