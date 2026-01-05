package com.sylphy.controller;

import com.sylphy.common.PageResult;
import com.sylphy.common.RedisCache;
import com.sylphy.common.Result;
import com.sylphy.dto.WaybillCreateDTO;
import com.sylphy.dto.WaybillQueryDTO;
import com.sylphy.service.WaybillService;
import com.sylphy.vo.WaybillVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 运单 Controller
 *
 * @author apple
 * @since 2026/1/4
 */
@Slf4j
@RestController
@RequestMapping("/waybill")
public class WaybillController {
    
    private final WaybillService waybillService;
    private final RedisCache redisCache;
    
    public WaybillController(WaybillService waybillService, RedisCache redisCache) {
        this.waybillService = waybillService;
        this.redisCache = redisCache;
    }
    
    /**
     * 创建运单
     */
    @PostMapping("/create")
    public Result<Long> createWaybill(@RequestHeader("Authorization") String token,
                                      @Valid @RequestBody WaybillCreateDTO createDTO) {
        Long consignorId = redisCache.getConsignorIdByToken(token);
        if (consignorId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        
        // 刷新 token
        redisCache.refreshToken(token);
        
        Long waybillId = waybillService.createWaybill(consignorId, createDTO);
        return Result.success(waybillId);
    }
    
    /**
     * 获取运单详情
     */
    @GetMapping("/{waybillId}")
    public Result<WaybillVO> getWaybillDetail(@RequestHeader("Authorization") String token,
                                              @PathVariable Long waybillId) {
        Long consignorId = redisCache.getConsignorIdByToken(token);
        if (consignorId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        
        // 刷新 token
        redisCache.refreshToken(token);
        
        WaybillVO waybillVO = waybillService.getWaybillDetail(waybillId, consignorId);
        return Result.success(waybillVO);
    }
    
    /**
     * 查询运单列表
     */
    @PostMapping("/list")
    public Result<PageResult<WaybillVO>> queryWaybills(@RequestHeader("Authorization") String token,
                                                       @RequestBody WaybillQueryDTO queryDTO) {
        Long consignorId = redisCache.getConsignorIdByToken(token);
        if (consignorId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        
        // 刷新 token
        redisCache.refreshToken(token);
        
        PageResult<WaybillVO> result = waybillService.queryConsignorWaybills(consignorId, queryDTO);
        return Result.success(result);
    }
    
    /**
     * 根据状态查询运单
     */
    @GetMapping("/listByStatus")
    public Result<PageResult<WaybillVO>> queryByStatus(@RequestHeader("Authorization") String token,
                                                       @RequestParam(required = false) Integer status,
                                                       @RequestParam(defaultValue = "1") Long current,
                                                       @RequestParam(defaultValue = "10") Long size) {
        Long consignorId = redisCache.getConsignorIdByToken(token);
        if (consignorId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        redisCache.refreshToken(token);
        
        WaybillQueryDTO queryDTO = new WaybillQueryDTO();
        queryDTO.setStatus(status);
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);
        
        PageResult<WaybillVO> result = waybillService.queryConsignorWaybills(consignorId, queryDTO);
        return Result.success(result);
    }
    
    /**
     * 根据时间查询运单
     */
    @GetMapping("/listByTime")
    public Result<PageResult<WaybillVO>> queryByTime(@RequestHeader("Authorization") String token,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
                                                     @RequestParam(defaultValue = "1") Long current,
                                                     @RequestParam(defaultValue = "10") Long size) {
        Long consignorId = redisCache.getConsignorIdByToken(token);
        if (consignorId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        redisCache.refreshToken(token);
        
        WaybillQueryDTO queryDTO = new WaybillQueryDTO();
        queryDTO.setStartTime(startTime);
        queryDTO.setEndTime(endTime);
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);
        
        PageResult<WaybillVO> result = waybillService.queryConsignorWaybills(consignorId, queryDTO);
        return Result.success(result);
    }

    /**
     * 根据地址查询运单
     */
    @GetMapping("/listByAddress")
    public Result<PageResult<WaybillVO>> queryByAddress(@RequestHeader("Authorization") String token,
                                                        @RequestParam(required = false) String startAddress,
                                                        @RequestParam(required = false) String endAddress,
                                                        @RequestParam(defaultValue = "1") Long current,
                                                        @RequestParam(defaultValue = "10") Long size) {
        Long consignorId = redisCache.getConsignorIdByToken(token);
        if (consignorId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        redisCache.refreshToken(token);

        WaybillQueryDTO queryDTO = new WaybillQueryDTO();
        queryDTO.setStartAddress(startAddress);
        queryDTO.setEndAddress(endAddress);
        queryDTO.setCurrent(current);
        queryDTO.setSize(size);

        PageResult<WaybillVO> result = waybillService.queryConsignorWaybills(consignorId, queryDTO);
        return Result.success(result);
    }
    
    /**
     * 取消运单
     */
    @DeleteMapping("/{waybillId}")
    public Result cancelWaybill(@RequestHeader("Authorization") String token,
                                @PathVariable Long waybillId) {
        Long consignorId = redisCache.getConsignorIdByToken(token);
        if (consignorId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        
        // 刷新 token
        redisCache.refreshToken(token);
        
        waybillService.cancelWaybill(waybillId, consignorId);
        return Result.success("取消运单成功");
    }
}
