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
import org.springframework.web.bind.annotation.*;

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
     * 发布运单
     */
    @PostMapping("/create")
    public Result<Long> createWaybill(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody WaybillCreateDTO createDTO) {
        // 从 Token 获取货主ID
        Long consignorId = redisCache.getConsignorIdByToken(token);
        if (consignorId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }

        Long waybillId = waybillService.createWaybill(consignorId, createDTO);
        return Result.success("运单创建成功", waybillId);
    }
    
    /**
     * 查询运单详情
     */
    @GetMapping("/{waybillId}")
    public Result<WaybillVO> getWaybillDetail(@RequestHeader("Authorization") String token,
                                              @PathVariable Long waybillId) {
        Long consignorId = redisCache.getConsignorIdByToken(token);
        if (consignorId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        WaybillVO waybillVO = waybillService.getWaybillDetail(waybillId,consignorId);
        return Result.success(waybillVO);
    }
    
    /**
     * 查询货主的历史运单（分页）
     */
    @PostMapping("/list")
    public Result<PageResult<WaybillVO>> queryWaybills(
            @RequestHeader("Authorization") String token,
            @RequestBody WaybillQueryDTO queryDTO) {
        // 从 Token 获取货主ID
        Long consignorId = redisCache.getConsignorIdByToken(token);
        if (consignorId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        
        PageResult<WaybillVO> result = waybillService.queryConsignorWaybills(consignorId, queryDTO);
        return Result.success(result);
    }
    
    /**
     * 取消运单
     */
    @DeleteMapping("/{waybillId}")
    public Result cancelWaybill(
            @RequestHeader("Authorization") String token,
            @PathVariable Long waybillId) {
        // 从 Token 获取货主ID
        Long consignorId = redisCache.getConsignorIdByToken(token);
        if (consignorId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        
        waybillService.cancelWaybill(waybillId, consignorId);
        return Result.success("运单取消成功");
    }
}
