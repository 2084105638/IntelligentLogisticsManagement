package com.sylphy.vo;

import lombok.Data;

@Data
public class DriverLoginVO {
    private String token;
    private Long driverId;
    private Long userId;
    private String email;
    private String phone;
}
