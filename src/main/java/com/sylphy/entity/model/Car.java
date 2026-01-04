package com.sylphy.entity.model;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * sys_car
 */
@Data
@TableName("sys_car")
public class Car implements Serializable {
    /**
     * 车辆主键 id
     */
    @TableId("car_id")
    private Long carId;
    
    /**
     * 车辆位置
     */
    @TableField("location")
    private String location;
    
    /**
     * 车辆状态 空闲中:0 使用中:1 维修中:2
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 关联的司机 id
     */
    @TableField("driver_id")
    private Long driverId;
    
    private static final long serialVersionUID = 1L;
}