package com.sylphy.entity.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * sys_waybill_assignment
 */
@Data
public class WaybillAssignment implements Serializable {
    /**
     * 运单操作 id
     */
    private Long assignmentId;
    
    /**
     * 操作者 id
     */
    private Long operatorId;
    
    /**
     * 修改前运单的id
     */
    private Long oldWaybillId;
    
    /**
     * 修改后运单的 id
     */
    private Long newWaybillId;
    
    /**
     * 执行时间
     */
    private Date create_time;
    
    private static final long serialVersionUID = 1L;
}