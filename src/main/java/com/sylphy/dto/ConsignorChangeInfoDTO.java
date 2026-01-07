package com.sylphy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author apple
 * @since 2026/1/7 17:38
 */
@Data
public class ConsignorChangeInfoDTO {
    private String email;
    
    private String newPassword;
    
    private String oldPassword;
}
