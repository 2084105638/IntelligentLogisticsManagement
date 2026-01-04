package com.sylphy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 货主登录 DTO
 *
 * @author apple
 * @since 2026/1/3
 */
@Data
public class ConsignorLoginDTO {

    @NotBlank(message = "手机号或邮箱不能为空")
    private String account;

    @NotBlank(message = "密码不能为空")
    private String password;
}
