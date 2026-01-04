package com.sylphy.dto;

import lombok.Data;

/**
 * 运单查询 DTO
 *
 * @author apple
 * @since 2026/1/4
 */
@Data
public class WaybillQueryDTO {

    /**
     * 运单状态 待分配:0 已分配:1 运输中:2 已完成:3
     */
    private Integer status;

    /**
     * 起始地址（模糊查询）
     */
    private String startAddress;

    /**
     * 结束地址（模糊查询）
     */
    private String endAddress;

    /**
     * 页码
     */
    private Long current = 1L;

    /**
     * 每页大小
     */
    private Long size = 10L;
}
