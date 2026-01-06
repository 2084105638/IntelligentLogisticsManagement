package com.sylphy.controller;

import com.sylphy.common.PageResult;
import com.sylphy.common.RedisCache;
import com.sylphy.common.Result;
import com.sylphy.dto.DriverLoginDTO;
import com.sylphy.dto.DriverRegisterDTO;
import com.sylphy.dto.WaybillQueryDTO;
import com.sylphy.common.WaybillStatus;
import com.sylphy.dto.WaybillExceptionReportDTO;
import com.sylphy.service.AssignmentService;
import com.sylphy.service.DriverService;
import com.sylphy.service.WaybillExceptionService;
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
    private final AssignmentService assignmentService;
    private final WaybillExceptionService exceptionService;
    private final RedisCache redisCache;
    
    public DriverController(DriverService driverService, AssignmentService assignmentService, WaybillExceptionService exceptionService, RedisCache redisCache) {
        this.driverService = driverService;
        this.assignmentService = assignmentService;
        this.exceptionService = exceptionService;
        this.redisCache = redisCache;
    }

    /**
     * 上报运单异常
     */
    @PostMapping("/exception")
    public Result reportException(@RequestHeader("Authorization") String token,
                                  @Valid @RequestBody WaybillExceptionReportDTO reportDTO) {
        Long driverId = redisCache.getDriverIdByToken(token);
        if (driverId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        redisCache.refreshToken(token);
        
        exceptionService.reportException(driverId, reportDTO);
        return Result.success("异常上报成功");
    }

    /**
     * 开始运输任务
     */
    @PostMapping("/start/{waybillId}")
    public Result startWaybill(@RequestHeader("Authorization") String token,
                               @PathVariable Long waybillId) {
        Long driverId = redisCache.getDriverIdByToken(token);
        if (driverId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        redisCache.refreshToken(token);

        // 这里的 operatorId 传入 driverId，但在逻辑中并未严格校验“司机-运单”归属，
        // 而是依赖 Service 层对运单状态流转的校验。
        // 如需严格校验，可先查询运单详情判断 carId 归属。
        // 目前采用简化逻辑：
        assignmentService.changeStatus(waybillId, WaybillStatus.IN_TRANSIT.getCode(), driverId);
        return Result.success("已开始运输");
    }

    /**
     * 结束运输任务
     */
    @PostMapping("/complete/{waybillId}")
    public Result completeWaybill(@RequestHeader("Authorization") String token,
                                  @PathVariable Long waybillId) {
        Long driverId = redisCache.getDriverIdByToken(token);
        if (driverId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        redisCache.refreshToken(token);

        assignmentService.changeStatus(waybillId, WaybillStatus.COMPLETED.getCode(), driverId);
        return Result.success("已完成运输");
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
