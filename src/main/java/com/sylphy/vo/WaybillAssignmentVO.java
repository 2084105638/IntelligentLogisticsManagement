package com.sylphy.vo;

import lombok.Data;

import java.util.Date;

@Data
public class WaybillAssignmentVO {
    private Long assignmentId;
    private Long operatorId;
    private Long oldWaybillId;
    private Long newWaybillId;
    private Date create_time;
}
