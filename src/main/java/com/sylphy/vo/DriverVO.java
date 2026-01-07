package com.sylphy.vo;

import lombok.Data;

/**
 * 司机信息 VO
 */
@Data
public class DriverVO {
    /**
     * 司机 ID
     */
    private Long driverId;
    
    /**
     * 关联用户 ID
     */
    private Long userId;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
}
