package com.sylphy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户创建 DTO
 *
 * @author apple
 * @since 2026/1/7
 */
@Data
public class UserCreateDTO {
    /**
     * 用户类型 1:货主 2:调度员 3:司机
     */
    @NotNull(message = "用户类型不能为空")
    private Integer type;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 邮箱（货主、司机必填）
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号（货主、司机必填，调度员可选）
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}

