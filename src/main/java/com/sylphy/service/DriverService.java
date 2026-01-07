package com.sylphy.service;

import com.sylphy.common.PageResult;
import com.sylphy.dto.DriverLocationUploadDTO;
import com.sylphy.dto.DriverLoginDTO;
import com.sylphy.dto.DriverRegisterDTO;
import com.sylphy.dto.WaybillQueryDTO;
import com.sylphy.vo.DriverLoginVO;
import com.sylphy.vo.WaybillVO;
import jakarta.validation.Valid;

/**
 * @author apple
 * @since 2026/1/5 08:38
 */
public interface DriverService {
    void register(@Valid DriverRegisterDTO registerDTO);
    
    DriverLoginVO login(@Valid DriverLoginDTO loginDTO);

    PageResult<WaybillVO> queryWaybills(Long driverId, WaybillQueryDTO queryDTO);

    /**
     * 上传司机/车辆位置
     *
     * @param driverId 司机ID
     * @param dto      位置信息
     */
    void uploadLocation(Long driverId, DriverLocationUploadDTO dto);
}
