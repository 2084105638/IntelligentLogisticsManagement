package com.sylphy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CarCreateDTO {
    private Long driverId;
    private String location;
    private Integer status;
}
