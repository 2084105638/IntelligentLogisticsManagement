package com.sylphy.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author apple
 * @since 2026/1/5 08:44
 */
@Data
@TableName("sys_driver")
public class Driver {
    /**
     * 司机主键 id
     */
    @TableId(value = "driver_id", type = IdType.ASSIGN_ID)
    private Long driverId;
    
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
}
