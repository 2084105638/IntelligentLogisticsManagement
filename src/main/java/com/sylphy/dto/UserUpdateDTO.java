package com.sylphy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户更新 DTO
 *
 * @author apple
 * @since 2026/1/7
 */
@Data
public class UserUpdateDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（为空则不修改）
     */
    private String password;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}

