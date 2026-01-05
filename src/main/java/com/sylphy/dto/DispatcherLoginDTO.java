package com.sylphy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DispatcherLoginDTO {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
