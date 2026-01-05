package com.sylphy.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_dispatcher")
public class Dispatcher implements Serializable {
    
    @TableId(value = "dispatcher_id", type = IdType.ASSIGN_ID)
    private Long dispatcherId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("password")
    private String password;
    
    @TableField("email")
    private String email;
    
    @TableField("phone")
    private String phone;
    
    @TableField("status")
    private Integer status;
    
    @TableField("create_time")
    private Date createTime;
    
    private static final long serialVersionUID = 1L;
}
