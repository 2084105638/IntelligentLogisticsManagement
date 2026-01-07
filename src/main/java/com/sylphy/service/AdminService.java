package com.sylphy.service;

import com.sylphy.common.PageResult;
import com.sylphy.dto.AdminLoginDTO;
import com.sylphy.dto.UserCreateDTO;
import com.sylphy.dto.UserQueryDTO;
import com.sylphy.dto.UserUpdateDTO;
import com.sylphy.vo.AdminLoginVO;
import com.sylphy.vo.UserVO;

/**
 * 管理员 Service 接口
 *
 * @author apple
 * @since 2026/1/7
 */
public interface AdminService {

    /**
     * 管理员登录
     *
     * @param loginDTO 登录信息
     * @return 登录响应
     */
    AdminLoginVO login(AdminLoginDTO loginDTO);

    /**
     * 管理员登出
     *
     * @param token Token
     */
    void logout(String token);

    /**
     * 查询用户列表（分页）
     *
     * @param queryDTO 查询条件
     * @return 用户列表
     */
    PageResult<UserVO> queryUsers(UserQueryDTO queryDTO);

    /**
     * 创建用户（货主、调度员、司机）
     *
     * @param createDTO 用户信息
     * @return 用户ID
     */
    Long createUser(UserCreateDTO createDTO);

    /**
     * 更新用户信息
     *
     * @param userId     用户ID
     * @param updateDTO  更新信息
     */
    void updateUser(Long userId, UserUpdateDTO updateDTO);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    void deleteUser(Long userId);

    /**
     * 禁用/启用用户
     *
     * @param userId 用户ID
     * @param status 状态 0:禁用 1:启用
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 根据Token获取管理员ID
     *
     * @param token Token
     * @return 管理员ID
     */
    Long getAdminIdByToken(String token);
}

