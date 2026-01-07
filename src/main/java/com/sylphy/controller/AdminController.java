package com.sylphy.controller;

import com.sylphy.common.PageResult;
import com.sylphy.common.RedisCache;
import com.sylphy.common.Result;
import com.sylphy.dto.AdminLoginDTO;
import com.sylphy.dto.UserCreateDTO;
import com.sylphy.dto.UserQueryDTO;
import com.sylphy.dto.UserUpdateDTO;
import com.sylphy.service.AdminService;
import com.sylphy.vo.AdminLoginVO;
import com.sylphy.vo.UserVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员 Controller
 *
 * @author apple
 * @since 2026/1/7
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final RedisCache redisCache;

    public AdminController(AdminService adminService, RedisCache redisCache) {
        this.adminService = adminService;
        this.redisCache = redisCache;
    }

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<AdminLoginVO> login(@Valid @RequestBody AdminLoginDTO loginDTO) {
        AdminLoginVO loginVO = adminService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 管理员登出
     */
    @PostMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token) {
        adminService.logout(token);
        return Result.success("登出成功");
    }

    /**
     * 查询用户列表（分页）
     */
    @PostMapping("/users/query")
    public Result<PageResult<UserVO>> queryUsers(@RequestHeader("Authorization") String token,
                                                  @RequestBody UserQueryDTO queryDTO) {
        // 验证管理员身份
        Long adminId = adminService.getAdminIdByToken(token);
        if (adminId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        redisCache.refreshToken(token);

        PageResult<UserVO> result = adminService.queryUsers(queryDTO);
        return Result.success(result);
    }

    /**
     * 创建用户（货主、调度员、司机）
     */
    @PostMapping("/users/create")
    public Result<Long> createUser(@RequestHeader("Authorization") String token,
                                   @Valid @RequestBody UserCreateDTO createDTO) {
        // 验证管理员身份
        Long adminId = adminService.getAdminIdByToken(token);
        if (adminId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        redisCache.refreshToken(token);

        Long userId = adminService.createUser(createDTO);
        return Result.success(userId);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/users/{userId}")
    public Result updateUser(@RequestHeader("Authorization") String token,
                            @PathVariable Long userId,
                            @Valid @RequestBody UserUpdateDTO updateDTO) {
        // 验证管理员身份
        Long adminId = adminService.getAdminIdByToken(token);
        if (adminId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        redisCache.refreshToken(token);

        adminService.updateUser(userId, updateDTO);
        return Result.success("更新成功");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{userId}")
    public Result deleteUser(@RequestHeader("Authorization") String token,
                            @PathVariable Long userId) {
        // 验证管理员身份
        Long adminId = adminService.getAdminIdByToken(token);
        if (adminId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        redisCache.refreshToken(token);

        adminService.deleteUser(userId);
        return Result.success("删除成功");
    }

    /**
     * 禁用/启用用户
     */
    @PutMapping("/users/{userId}/status")
    public Result updateUserStatus(@RequestHeader("Authorization") String token,
                                   @PathVariable Long userId,
                                   @RequestParam Integer status) {
        // 验证管理员身份
        Long adminId = adminService.getAdminIdByToken(token);
        if (adminId == null) {
            return Result.error(401, "Token 已过期，请重新登录");
        }
        redisCache.refreshToken(token);

        adminService.updateUserStatus(userId, status);
        return Result.success("状态更新成功");
    }
}
