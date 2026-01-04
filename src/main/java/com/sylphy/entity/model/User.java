package com.sylphy.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * sys_user
 *
 * @author apple
 */
@Data
@TableName("sys_user")
public class User implements Serializable {
    /**
     * 用户主键id
     */
    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    private Long userId;
    
    /**
     * 用户类型 0:管理员 1:货主 2:调度员 3:司机
     */
    @TableField("type")
    private Integer type;
    
    /**
     * 用户名
     */
    @TableField("username")
    private String username;
    
    private static final long serialVersionUID = 1L;
}