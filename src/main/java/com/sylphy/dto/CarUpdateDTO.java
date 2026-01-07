package com.sylphy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CarUpdateDTO {
    @NotNull
    private Long carId;
    private Long driverId;
    private String location;
    private Integer status;
    private Integer type;
}
