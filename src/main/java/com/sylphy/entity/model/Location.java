package com.sylphy.entity.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 运单历史位置记录
 */
@Data
@TableName("sys_location")
public class Location implements Serializable {
    /**
     * 位置主键 id
     */
    @TableId(value = "location_id", type = IdType.ASSIGN_ID)
    private Long locationId;
    
    /**
     * 关联运单 id
     */
    @TableField("waybill_identification")
    private Long waybillIdentification;
    
    /**
     * 位置信息
     */
    @TableField("location_info")
    private String locationInfo;
    
    /**
     * 位置上传时间
     */
    @TableField("location_date")
    private Date locationDate;
    
    private static final long serialVersionUID = 1L;
}
