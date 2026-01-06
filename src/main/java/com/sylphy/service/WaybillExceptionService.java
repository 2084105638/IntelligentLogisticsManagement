package com.sylphy.service;

import com.sylphy.dto.WaybillExceptionReportDTO;

/**
 * 运单异常 Service 接口
 */
public interface WaybillExceptionService {
    
    /**
     * 上报运单异常
     *
     * @param driverId 司机ID
     * @param dto      异常信息
     */
    void reportException(Long driverId, WaybillExceptionReportDTO dto);
}
