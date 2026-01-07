package com.sylphy.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 运单异常实体
 */
@Data
@TableName("sys_waybill_exception")
public class WaybillException implements Serializable {
    /**
     * 异常主键 id
     */
    @TableId(value = "exception_id", type = IdType.ASSIGN_ID)
    private Long exceptionId;

    /**
     * 关联的运单 id
     */
    @TableField("waybill_identification")
    private Long waybillIdentification;

    /**
     * 车辆id
     */
    @TableField("car_id")
    private Long carId;

    /**
     * 异常描述
     */
    @TableField("description")
    private String description;

    /**
     * 异常时间
     */
    @TableField("exception_date")
    private Date exceptionDate;

    private static final long serialVersionUID = 1L;
}
