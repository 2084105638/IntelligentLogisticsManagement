package com.sylphy.entity.model;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * sys_waybill_assignment
 */
@Data
@TableName("sys_waybill_assignment")
public class WaybillAssignment implements Serializable {
    /**
     * 运单操作 id
     */
    @TableId("assignment_id")
    private Long assignmentId;
    
    /**
     * 操作者 id
     */
    @TableField("operator_id")
    private Long operatorId;
    
    /**
     * 修改前运单的id
     */
    @TableField("old_waybill_id")
    private Long oldWaybillId;
    
    /**
     * 修改后运单的 id
     */
    @TableField("new_waybill_id")
    private Long newWaybillId;
    
    /**
     * 执行时间
     */
    @TableField("create_time")
    private Date createTime;
    
    private static final long serialVersionUID = 1L;
}