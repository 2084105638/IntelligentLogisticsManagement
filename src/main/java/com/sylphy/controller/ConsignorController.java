package com.sylphy.controller;

import com.sylphy.common.RedisCache;
import com.sylphy.common.Result;
import com.sylphy.dto.ConsignorChangeInfoDTO;
import com.sylphy.dto.ConsignorLoginDTO;
import com.sylphy.dto.ConsignorRegisterDTO;
import com.sylphy.entity.model.Consignor;
import com.sylphy.service.ConsignorService;
import com.sylphy.vo.ConsignorLoginVO;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 货主 Controller
 *
 * @author apple
 * @since 2026/1/3
 */
@Slf4j
@RestController
@RequestMapping("/consignor")
public class ConsignorController {

    private final ConsignorService consignorService;
    private final RedisCache redisCache;

    public ConsignorController(ConsignorService consignorService, RedisCache redisCache) {
        this.consignorService = consignorService;
        this.redisCache = redisCache;
    }

    /**
     * 货主注册
     */
    @PostMapping("/register")
    public Result register(@Valid @RequestBody ConsignorRegisterDTO registerDTO) {
        consignorService.register(registerDTO);
        return Result.success("注册成功");
    }

    /**
     * 货主登录
     */
    @PostMapping("/login")
    public Result<ConsignorLoginVO> login(@Valid @RequestBody ConsignorLoginDTO loginDTO) {
        ConsignorLoginVO loginVO = consignorService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 货主登出
     */
    @PostMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token) {
        consignorService.logout(token);
        return Result.success("登出成功");
    }

    /**
     * 获取当前登录货主信息
     */
    @GetMapping("/info")
    public Result<Consignor> getCurrentConsignor(@RequestHeader("Authorization") String token) {
        Consignor consignor = consignorService.getConsignorByToken(token);
        // 清空密码字段，不返回给前端
        consignor.setPassword(null);
        return Result.success(consignor);
    }
    
    /**
     * 修改货主信息
     */
    @PostMapping("/changeInfo")
    public Result<Consignor> changeInfo(@RequestHeader("Authorization") String token,
                                        @Valid @RequestBody ConsignorChangeInfoDTO consignorChangeInfoDTO){
        Long consignorId = redisCache.getConsignorIdByToken(token);
        if (consignorId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        redisCache.refreshToken(token);

        Consignor consignor = consignorService.updateConsignor(consignorId, consignorChangeInfoDTO);
        // 清空密码字段
        consignor.setPassword(null);
        return Result.success(consignor);
    }

    /**
     * 查询所有货主（收货人）
     */
    @GetMapping("/list")
    public Result<List<Consignor>> listAllConsignors(@RequestHeader("Authorization") String token) {
        Long consignorId = redisCache.getConsignorIdByToken(token);
        if (consignorId == null) {
            // 这里我们假设只有货主能查？或者调度员也能查？
            // 鉴权逻辑：这里暂时只校验Token有效性，若需要区分角色请扩展
            // 尝试校验是否是货主登录，或者是否是调度员等。
            // 简单处理：只要有有效Token（Redis中有记录）即可。
            // 注意：RedisCache 中不同角色存储 Key 可能不同，这里 getConsignorIdByToken 只能取到货主。
            // 如果调度员也要查，可能需要更通用的鉴权。
            // 既然是 ConsignorController，通常面向货主端。
            return Result.error(401, "Token 已过期，请重新登录");
        }
        redisCache.refreshToken(token);

        return Result.success(consignorService.listAllConsignors());
    }
}
