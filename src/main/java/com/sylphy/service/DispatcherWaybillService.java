package com.sylphy.service;

import com.sylphy.common.PageResult;
import com.sylphy.dto.DispatcherWaybillQueryDTO;
import com.sylphy.vo.WaybillVO;

public interface DispatcherWaybillService {

    PageResult<WaybillVO> queryWaybillsForDispatcher(DispatcherWaybillQueryDTO queryDTO);

    WaybillVO getWaybillDetailForDispatcher(Long waybillId);
}
