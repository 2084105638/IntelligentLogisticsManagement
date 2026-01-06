package com.sylphy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * 运单异常上报 DTO
 */
@Data
public class WaybillExceptionReportDTO {
    
    @NotNull(message = "运单ID不能为空")
    private Long waybillId;
    
    @NotBlank(message = "异常描述不能为空")
    private String description;
    
    @NotNull(message = "异常发生时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date exceptionDate;
}
