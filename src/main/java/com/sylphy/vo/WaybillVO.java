package com.sylphy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 运单详情 VO
 *
 * @author apple
 * @since 2026/1/4
 */
@Data
public class WaybillVO {

    /**
     * 运单主键 id
     */
    private Long waybillId;

    /**
     * 货主 ID
     */
    private Long consignorId;

    /**
     * 货物信息
     */
    private String goodsInformation;

    /**
     * 起始地址
     */
    private String startAddress;

    /**
     * 结束地址
     */
    private String endAddress;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 期望时效
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expectedTimeLimit;

    /**
     * 费用
     */
    private BigDecimal cost;

    /**
     * 状态 待分配:0 已分配:1 运输中:2 已完成:3
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;
}
