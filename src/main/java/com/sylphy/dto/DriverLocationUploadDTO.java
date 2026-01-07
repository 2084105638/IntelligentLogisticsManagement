package com.sylphy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 司机位置上传 DTO
 */
@Data
public class DriverLocationUploadDTO {
    
    /**
     * 当前位置（如坐标或地址描述）
     */
    @NotBlank(message = "位置信息不能为空")
    private String location;
    
    /**
     * 当前关联的运单唯一标识ID（可选，若为空则表示空载）
     */
    private Long waybillIdentification;

}
