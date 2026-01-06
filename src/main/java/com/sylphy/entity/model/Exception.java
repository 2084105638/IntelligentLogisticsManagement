package com.sylphy.entity.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * sys_exception
 */
@Data
public class Exception implements Serializable {
    /**
     * 异常主键 id
     */
    private Long exceptionId;
    
    /**
     * 关联的运单 id
     */
    private Long waybillId;
    
    
    /**
     * 异常时间
     */
    private Date exceptionDate;
    
    private static final long serialVersionUID = 1L;
}