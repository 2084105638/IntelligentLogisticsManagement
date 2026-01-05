package com.sylphy.service;

import com.sylphy.dto.DriverLoginDTO;
import com.sylphy.dto.DriverRegisterDTO;
import com.sylphy.vo.DriverLoginVO;
import jakarta.validation.Valid;

/**
 * @author apple
 * @since 2026/1/5 08:38
 */
public interface DriverService {
    void register(@Valid DriverRegisterDTO registerDTO);
    
    DriverLoginVO login(@Valid DriverLoginDTO loginDTO);
}
