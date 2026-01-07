package com.sylphy.controller;

import com.sylphy.common.PageResult;
import com.sylphy.common.RedisCache;
import com.sylphy.common.Result;
import com.sylphy.common.WaybillStatus;
import com.sylphy.dto.*;
import com.sylphy.entity.model.Dispatcher;
import com.sylphy.service.*;
import com.sylphy.vo.CarVO;
import com.sylphy.vo.DispatcherLoginVO;
import com.sylphy.vo.DriverVO;
import com.sylphy.vo.WaybillAssignmentVO;
import com.sylphy.vo.WaybillVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/dispatcher")
public class DispatcherController {
    
    private final DispatcherService dispatcherService;
    private final DispatcherWaybillService dispatcherWaybillService;
    private final CarService carService;
    private final DriverService driverService;
    private final AssignmentService assignmentService;
    private final RedisCache redisCache;
    
    public DispatcherController(DispatcherService dispatcherService,
                                DispatcherWaybillService dispatcherWaybillService,
                                CarService carService,
                                DriverService driverService,
                                AssignmentService assignmentService,
                                RedisCache redisCache) {
        this.dispatcherService = dispatcherService;
        this.dispatcherWaybillService = dispatcherWaybillService;
        this.carService = carService;
        this.driverService = driverService;
        this.assignmentService = assignmentService;
        this.redisCache = redisCache;
    }
    
    @PostMapping("/login")
    /**
     * 调度员登录
     * @param loginDTO 登录请求体(DispatcherLoginDTO，包含用户名与明文密码)
     * @return 登录成功返回包含token与调度员基础信息的结果集(Result < DispatcherLoginVO >)
     * @throws com.sylphy.exception.BusinessException 当账号不存在或密码错误时抛出
     * @author apple
     * @since 1.0.0
     */
    public Result<DispatcherLoginVO> login(@Valid @RequestBody DispatcherLoginDTO loginDTO) {
        DispatcherLoginVO vo = dispatcherService.login(loginDTO);
        return Result.success(vo);
    }
    
    @PostMapping("/logout")
    /**
     * 调度员登出
     * @param token 请求头携带的认证token(String类型，用于标识当前会话)
     * @return 操作结果(Result < String >)，成功返回“登出成功”
     * @throws com.sylphy.exception.BusinessException 当redis状态异常时可能抛出
     * @author apple
     * @since 1.0.0
     */
    public Result logout(@RequestHeader("Authorization") String token) {
        // 调用服务层清理token与调度员缓存
        dispatcherService.logout(token);
        return Result.success("登出成功");
    }
    
    @GetMapping("/me")
    /**
     * 获取当前登录调度员信息
     * @param token 请求头携带的认证token(String类型)
     * @return 调度员实体(Result < Dispatcher >)，密码字段已置空
     * @throws com.sylphy.exception.BusinessException 当token过期或用户不存在时抛出
     * @author apple
     * @since 1.0.0
     */
    public Result<Dispatcher> me(@RequestHeader("Authorization") String token) {
        Dispatcher dispatcher = dispatcherService.getDispatcherByToken(token);
        // 安全处理：不返回敏感字段
        dispatcher.setPassword(null);
        return Result.success(dispatcher);
    }
    
    
    @GetMapping("/waybills/{waybillId}")
    /**
     * 运单详情（调度员视角）
     * @param token 请求头认证token(String)
     * @param waybillId 运单ID(Long类型)
     * @return 运单详情(Result < WaybillVO >)
     * @throws com.sylphy.exception.BusinessException 当运单不存在时抛出
     * @author apple
     * @since 1.0.0
     */
    
    public Result<WaybillVO> getWaybill(@RequestHeader("Authorization") String token,
                                        @PathVariable Long waybillId) {
        Long dispatcherId = redisCache.getDispatcherIdByToken(token);
        if (dispatcherId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        return Result.success(dispatcherWaybillService.getWaybillDetailForDispatcher(waybillId));
    }
    
    @GetMapping("/waybills")
    /**
     * 运单列表（统一接口，按前端传入状态进行分类）
     * @param token 请求头认证token(String)
     * @param dto 查询参数(DispatcherWaybillQueryDTO，支持 status、起止地址、关键词、分页)
     * @return 分页结果(PageResult < WaybillVO >)，按创建时间倒序
     * @throws com.sylphy.exception.BusinessException 当查询过程发生业务错误时抛出
     * @author apple
     * @since 1.0.1
     */
    public Result<PageResult<WaybillVO>> listWaybills(@RequestHeader("Authorization") String token,
                                                      DispatcherWaybillQueryDTO dto) {
        Long dispatcherId = redisCache.getDispatcherIdByToken(token);
        if (dispatcherId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        return Result.success(dispatcherWaybillService.queryWaybillsForDispatcher(dto));
    }
    
    @PostMapping("/cars")
    /**
     * 创建车辆
     * @param token 请求头认证token(String)
     * @param dto 车辆创建参数(CarCreateDTO，包含司机、位置与初始状态)
     * @return 新增车辆ID(Result < Long >)
     * @throws com.sylphy.exception.BusinessException 当插入失败或参数非法时抛出
     * @author apple
     * @since 1.0.0
     */
    public Result<Long> createCar(@RequestHeader("Authorization") String token,
                                  @Valid @RequestBody CarCreateDTO dto) {
        Long dispatcherId = redisCache.getDispatcherIdByToken(token);
        if (dispatcherId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        return Result.success(carService.createCar(dto));
    }
    
    @PutMapping("/cars/{carId}")
    /**
     * 更新车辆信息
     * @param token 请求头认证token(String)
     * @param carId 路径参数-车辆ID(Long类型)
     * @param dto 车辆更新参数(CarUpdateDTO，可更新司机、位置、状态)
     * @return 操作结果(Result < String >)，成功返回“更新成功”
     * @throws com.sylphy.exception.BusinessException 当车辆不存在或更新失败时抛出
     * @author apple
     * @since 1.0.0
     */
    public Result updateCar(@RequestHeader("Authorization") String token,
                            @PathVariable Long carId,
                            @Valid @RequestBody CarUpdateDTO dto) {
        Long dispatcherId = redisCache.getDispatcherIdByToken(token);
        if (dispatcherId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        // 将路径参数写入更新DTO，确保更新目标明确
        dto.setCarId(carId);
        carService.updateCar(dto);
        return Result.success("更新成功");
    }
    
    @DeleteMapping("/cars/{carId}")
    /**
     * 删除车辆
     * @param token 请求头认证token(String)
     * @param carId 路径参数-车辆ID(Long类型)
     * @return 操作结果(Result < String >)，成功返回“删除成功”
     * @throws com.sylphy.exception.BusinessException 当车辆不存在或删除失败时抛出
     * @author apple
     * @since 1.0.0
     */
    public Result deleteCar(@RequestHeader("Authorization") String token,
                            @PathVariable Long carId) {
        Long dispatcherId = redisCache.getDispatcherIdByToken(token);
        if (dispatcherId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        carService.deleteCar(carId);
        return Result.success("删除成功");
    }
    
    @GetMapping("/cars")
    /**
     * 车辆分页查询
     * @param token 请求头认证token(String)
     * @param dto 查询参数(CarQueryDTO，支持按状态/司机/位置筛选与分页)
     * @return 分页结果(PageResult < CarVO >)
     * @throws com.sylphy.exception.BusinessException 查询异常时抛出
     * @author apple
     * @since 1.0.0
     */
    public Result<PageResult<CarVO>> listCars(@RequestHeader("Authorization") String token,
                                              CarQueryDTO dto) {
        Long dispatcherId = redisCache.getDispatcherIdByToken(token);
        if (dispatcherId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        return Result.success(carService.queryCars(dto));
    }
    
    @GetMapping("/cars/{carId}/status")
    /**
     * 获取车辆状态
     * @param token 请求头认证token(String)
     * @param carId 路径参数-车辆ID(Long类型)
     * @return 车辆状态(Result < Integer >)，约定：0-可用 1-已分配 2-维修中
     * @throws com.sylphy.exception.BusinessException 当车辆不存在时抛出
     * @author apple
     * @since 1.0.0
     */
    public Result<Integer> getCarStatus(@RequestHeader("Authorization") String token,
                                        @PathVariable Long carId) {
        Long dispatcherId = redisCache.getDispatcherIdByToken(token);
        if (dispatcherId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        return Result.success(carService.getStatus(carId));
    }
    
    @PutMapping("/cars/{carId}/status")
    /**
     * 更新车辆状态
     * @param token 请求头认证token(String)
     * @param carId 路径参数-车辆ID(Long类型)
     * @param status 请求参数-车辆状态(Integer类型：0-可用 1-已分配 2-维修中)
     * @return 操作结果(Result < String >)，成功返回“更新成功”
     * @throws com.sylphy.exception.BusinessException 当车辆不存在或更新失败时抛出
     * @author apple
     * @since 1.0.0
     */
    public Result updateCarStatus(@RequestHeader("Authorization") String token,
                                  @PathVariable Long carId,
                                  @RequestParam Integer status) {
        Long dispatcherId = redisCache.getDispatcherIdByToken(token);
        if (dispatcherId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        carService.updateStatus(carId, status);
        return Result.success("更新成功");
    }
    
    @GetMapping("/cars/{carId}/location")
    /**
     * 获取车辆当前位置
     * @param token 请求头认证token(String)
     * @param carId 路径参数-车辆ID(Long类型)
     * @return 车辆位置描述(Result < String >)，可为坐标或文本地址
     * @throws com.sylphy.exception.BusinessException 当车辆不存在时抛出
     * @author apple
     * @since 1.0.0
     */
    public Result<String> getCarLocation(@RequestHeader("Authorization") String token,
                                         @PathVariable Long carId) {
        Long dispatcherId = redisCache.getDispatcherIdByToken(token);
        if (dispatcherId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        return Result.success(carService.getLocation(carId));
    }
    
    @PutMapping("/cars/{carId}/location")
    /**
     * 更新车辆当前位置
     * @param token 请求头认证token(String)
     * @param carId 路径参数-车辆ID(Long类型)
     * @param location 请求参数-车辆位置(String类型，建议“lat,lng”或文本地址)
     * @return 操作结果(Result < String >)，成功返回“更新成功”
     * @throws com.sylphy.exception.BusinessException 当车辆不存在或更新失败时抛出
     * @author apple
     * @since 1.0.0
     */
    public Result updateCarLocation(@RequestHeader("Authorization") String token,
                                    @PathVariable Long carId,
                                    @RequestParam String location) {
        Long dispatcherId = redisCache.getDispatcherIdByToken(token);
        if (dispatcherId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        carService.updateLocation(carId, location);
        return Result.success("更新成功");
    }
    
    @PostMapping("/assign")
    /**
     * 手动分配车辆到运单（遵循新旧记录原则）
     * @param token 请求头认证token(String)
     * @param oldWaybillId 请求参数-旧运单ID(Long类型)，必须为待分配状态
     * @param carId 请求参数-车辆ID(Long类型)，必须为可用状态
     * @return 新运单ID(Result < Long >)，复制旧运单并设置车辆与状态
     * @throws com.sylphy.exception.BusinessException 状态不合法或资源不可用时抛出
     * @author apple
     * @since 1.0.0
     */
    public Result<Long> assign(@RequestHeader("Authorization") String token,
                               @RequestParam Long oldWaybillId,
                               @RequestParam Long carId) {
        Long dispatcherId = redisCache.getDispatcherIdByToken(token);
        if (dispatcherId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        // 调用服务：复制旧运单 -> 新运单设置carId与status -> 旧运单标记changed -> 写入历史
        Long newWaybillId = assignmentService.assignVehicle(oldWaybillId, carId, dispatcherId);
        return Result.success(newWaybillId);
    }
    
    @PostMapping("/assign/auto")
    /**
     * 自动匹配并分配车辆（遵循新旧记录原则）
     * @param token 请求头认证token(String)
     * @param oldWaybillId 请求参数-旧运单ID(Long类型)
     * @return 新运单ID(Result < Long >)
     * @throws com.sylphy.exception.BusinessException 当无可用车辆或状态不合法时抛出
     * @author apple
     * @since 1.0.0
     */
    public Result<Long> autoAssign(@RequestHeader("Authorization") String token,
                                   @RequestParam Long oldWaybillId) {
        Long dispatcherId = redisCache.getDispatcherIdByToken(token);
        if (dispatcherId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        // 简化算法：从可用车辆中选取一辆进行分配
        Long newWaybillId = assignmentService.autoMatchAndAssign(oldWaybillId, dispatcherId);
        return Result.success(newWaybillId);
    }
    
    @PostMapping("/assign/reassign")
    /**
     * 重分配车辆（遵循新旧记录原则）
     * @param token 请求头认证token(String)
     * @param oldWaybillId 请求参数-旧运单ID(Long类型)
     * @param newCarId 请求参数-新车辆ID(Long类型)
     * @return 新运单ID(Result < Long >)
     * @throws com.sylphy.exception.BusinessException 当新车辆不可用或状态不合法时抛出
     * @author apple
     * @since 1.0.0
     */
    public Result<Long> reassign(@RequestHeader("Authorization") String token,
                                 @RequestParam Long oldWaybillId,
                                 @RequestParam Long newCarId) {
        Long dispatcherId = redisCache.getDispatcherIdByToken(token);
        if (dispatcherId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        // 重分配：生成新运单并解除旧车辆占用
        Long newWaybillId = assignmentService.reassignVehicle(oldWaybillId, newCarId, dispatcherId);
        return Result.success(newWaybillId);
    }
    
    @GetMapping("/assign/history")
    /**
     * 分配/修改历史记录分页查询
     * @param token 请求头认证token(String)
     * @param oldWaybillId 可选-旧运单ID(Long类型)
     * @param newWaybillId 可选-新运单ID(Long类型)
     * @param operatorId 可选-操作者ID(Long类型，通常为调度员ID)
     * @param current 页码(Long类型，默认1)
     * @param size 每页大小(Long类型，默认10)
     * @return 分页结果(PageResult < WaybillAssignmentVO >)
     * @throws com.sylphy.exception.BusinessException 查询异常时抛出
     * @author apple
     * @since 1.0.0
     */
    public Result<PageResult<WaybillAssignmentVO>> history(@RequestHeader("Authorization") String token,
                                                           @RequestParam(required = false) Long oldWaybillId,
                                                           @RequestParam(required = false) Long newWaybillId,
                                                           @RequestParam(required = false) Long operatorId,
                                                           @RequestParam(defaultValue = "1") long current,
                                                           @RequestParam(defaultValue = "10") long size) {
        Long dispatcherId = redisCache.getDispatcherIdByToken(token);
        if (dispatcherId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        PageResult<WaybillAssignmentVO> result = assignmentService.listAssignments(oldWaybillId, newWaybillId, operatorId, current, size);
        return Result.success(result);
    }

    @PostMapping("/drivers")
    /**
     * 分页查询司机列表
     * @param token 请求头认证token(String)
     * @param queryDTO 查询条件(DriverQueryDTO)
     * @return 分页结果(PageResult < DriverVO >)
     * @throws com.sylphy.exception.BusinessException 查询异常时抛出
     * @author apple
     * @since 1.0.0
     */
    public Result<PageResult<DriverVO>> listDrivers(@RequestHeader("Authorization") String token,
                                                    @RequestBody DriverQueryDTO queryDTO) {
        Long dispatcherId = redisCache.getDispatcherIdByToken(token);
        if (dispatcherId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        return Result.success(driverService.queryDrivers(queryDTO));
    }
    
}
