package com.sylphy.dto;

import lombok.Data;

/**
 * 司机查询 DTO
 */
@Data
public class DriverQueryDTO {
    
    /**
     * 当前页
     */
    private Integer current = 1;
    
    /**
     * 每页条数
     */
    private Integer size = 10;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
}
