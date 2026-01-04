package com.sylphy.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 发布运单 DTO
 *
 * @author apple
 * @since 2026/1/4
 */
@Data
public class WaybillCreateDTO {

    @NotBlank(message = "货物信息不能为空")
    private String goodsInformation;

    @NotBlank(message = "起始地址不能为空")
    private String startAddress;

    @NotBlank(message = "结束地址不能为空")
    private String endAddress;

    @NotNull(message = "期望时效不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expectedTimeLimit;

    @NotNull(message = "费用不能为空")
    @DecimalMin(value = "0.01", message = "费用必须大于0")
    private BigDecimal cost;
}
