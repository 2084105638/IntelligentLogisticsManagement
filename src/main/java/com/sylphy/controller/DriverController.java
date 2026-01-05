package com.sylphy.controller;

import com.sylphy.common.PageResult;
import com.sylphy.common.RedisCache;
import com.sylphy.common.Result;
import com.sylphy.dto.DriverLoginDTO;
import com.sylphy.dto.DriverRegisterDTO;
import com.sylphy.dto.WaybillQueryDTO;
import com.sylphy.service.DriverService;
import com.sylphy.vo.DriverLoginVO;
import com.sylphy.vo.WaybillVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author apple
 * @since 2026/1/4 20:33
 */
@Slf4j
@RestController
@RequestMapping("/driver")
public class DriverController {
    private final DriverService driverService;
    private final RedisCache redisCache;
    
    public DriverController(DriverService driverService, RedisCache redisCache) {
        this.driverService = driverService;
        this.redisCache = redisCache;
    }
    
    /**
     * 司机注册
     */
    @PostMapping("/register")
    public Result register(@Valid @RequestBody DriverRegisterDTO registerDTO) {
        driverService.register(registerDTO);
        return Result.success("注册成功");
    }
    
    /**
     * 司机登录
     */
    @PostMapping("/login")
    public Result<DriverLoginVO> login(@Valid @RequestBody DriverLoginDTO loginDTO) {
        DriverLoginVO loginVO = driverService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 查询司机运单
     */
    @PostMapping("/waybills")
    public Result<PageResult<WaybillVO>> queryWaybills(@RequestHeader("Authorization") String token,
                                                       @RequestBody WaybillQueryDTO queryDTO) {
        Long driverId = redisCache.getDriverIdByToken(token);
        if (driverId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        redisCache.refreshToken(token);

        PageResult<WaybillVO> result = driverService.queryWaybills(driverId, queryDTO);
        return Result.success(result);
    }
    
}
