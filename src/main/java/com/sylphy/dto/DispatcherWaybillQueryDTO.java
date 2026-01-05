package com.sylphy.dto;

import lombok.Data;

@Data
public class DispatcherWaybillQueryDTO {
    private Integer status;
    private String startAddress;
    private String endAddress;
    private String keyword;
    private long current = 1;
    private long size = 10;
}
