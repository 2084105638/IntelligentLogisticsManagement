package com.sylphy.dto;

import lombok.Data;

@Data
public class CarQueryDTO {
    private Integer status;
    private Long driverId;
    private String location;
    private long current = 1;
    private long size = 10;
}
