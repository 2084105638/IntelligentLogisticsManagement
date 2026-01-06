package com.sylphy.entity.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.TypeHandler;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * sys_waybill
 */
@Data
@TableName("sys_waybill")
public class Waybill implements Serializable {
    /**
     * 运单主键 id
     */
    @TableId(value = "waybill_id", type = IdType.ASSIGN_ID)
    private Long waybillId;
    
    /**
     * 运单标识 id，运单标识 id，相同的运单不同的历史具有相同的 id
     */
    @TableField(value = "waybill_identification", fill = FieldFill.INSERT)
    private Long waybillIdentification;
    
    /**
     * 货物信息
     */
    @TableField("goods_information")
    private String goodsInformation;
    
    /**
     * 起始地址
     */
    @TableField("start_address")
    private String startAddress;
    
    /**
     * 结束地址
     */
    @TableField("end_address")
    private String endAddress;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;
    
    /**
     * 期望时效
     */
    @TableField("expected_time_limit")
    private Date expectedTimeLimit;
    
    /**
     * 费用
     */
    @TableField("cost")
    private BigDecimal cost;
    
    /**
     * 状态 待分配:0 已分配:1 运输中: 2 已完成:3
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 创建者id
     */
    @TableField("created_consignor")
    private Long createdConsignor;
    
    /**
     * 接收者 id
     */
    @TableField("receiving_consignor")
    private Long receivingConsignor;
    
    /**
     * 运输车辆 id
     */
    @TableField("car_id")
    private Long carId;
    
    /**
     * 是否已被修改 未被修改:0 已被修改:1
     */
    @TableField("changed")
    private Integer changed;
    
    private static final long serialVersionUID = 1L;
}