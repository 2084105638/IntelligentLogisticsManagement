package com.sylphy.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 货主登录响应 VO
 *
 * @author apple
 * @since 2026/1/3
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsignorLoginVO {

    /**
     * Token
     */
    private String token;

    /**
     * 货主ID
     */
    private Long consignorId;

    /**
     * 用户ID
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
}
