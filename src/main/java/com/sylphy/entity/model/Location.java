package com.sylphy.entity.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * sys_location
 */
@Data
public class Location implements Serializable {
    /**
     * 位置主键 id
     */
    private Long locationId;
    
    /**
     * 关联运单 id
     */
    private Long waybill;
    
    /**
     * 位置上传时间
     */
    private Date locationDate;
    
    private static final long serialVersionUID = 1L;
}