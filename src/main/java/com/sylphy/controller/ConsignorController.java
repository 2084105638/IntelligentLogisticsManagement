package com.sylphy.controller;

import com.sylphy.common.Result;
import com.sylphy.dto.ConsignorLoginDTO;
import com.sylphy.dto.ConsignorRegisterDTO;
import com.sylphy.entity.model.Consignor;
import com.sylphy.service.ConsignorService;
import com.sylphy.vo.ConsignorLoginVO;
import jakarta.validation.Valid;
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

    public ConsignorController(ConsignorService consignorService) {
        this.consignorService = consignorService;
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
}
