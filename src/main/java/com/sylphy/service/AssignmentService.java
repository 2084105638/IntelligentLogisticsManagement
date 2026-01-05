package com.sylphy.service;

import com.sylphy.common.PageResult;
import com.sylphy.vo.WaybillAssignmentVO;

public interface AssignmentService {
    Long assignVehicle(Long oldWaybillId, Long carId, Long operatorId);
    Long autoMatchAndAssign(Long oldWaybillId, Long operatorId);
    Long reassignVehicle(Long oldWaybillId, Long newCarId, Long operatorId);
    PageResult<WaybillAssignmentVO> listAssignments(Long oldWaybillId, Long newWaybillId, Long operatorId, long current, long size);
}
