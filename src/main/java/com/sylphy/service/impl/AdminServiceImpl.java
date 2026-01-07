package com.sylphy.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sylphy.common.PageResult;
import com.sylphy.common.RedisCache;
import com.sylphy.common.StringTools;
import com.sylphy.dto.AdminLoginDTO;
import com.sylphy.dto.UserCreateDTO;
import com.sylphy.dto.UserQueryDTO;
import com.sylphy.dto.UserUpdateDTO;
import com.sylphy.entity.model.*;
import com.sylphy.exception.BusinessException;
import com.sylphy.mapper.*;
import com.sylphy.service.AdminService;
import com.sylphy.vo.AdminLoginVO;
import com.sylphy.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员 Service 实现类
 *
 * @author apple
 * @since 2026/1/7
 */
@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    private final UserDao userDao;
    private final ConsignorDao consignorDao;
    private final DispatcherDao dispatcherDao;
    private final DriverDao driverDao;
    private final RedisCache redisCache;

    @Value("${admin.password}")
    private String adminPassword;

    public AdminServiceImpl(UserDao userDao, ConsignorDao consignorDao,
                           DispatcherDao dispatcherDao, DriverDao driverDao,
                           RedisCache redisCache) {
        this.userDao = userDao;
        this.consignorDao = consignorDao;
        this.dispatcherDao = dispatcherDao;
        this.driverDao = driverDao;
        this.redisCache = redisCache;
    }

    @Override
    public AdminLoginVO login(AdminLoginDTO loginDTO) {
        // 查询管理员用户
        User user = userDao.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginDTO.getUsername())
                .eq(User::getType, 0)); // 0:管理员

        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 验证密码（从application.yml配置中读取）
        if (adminPassword == null || adminPassword.isEmpty()) {
            throw new BusinessException("管理员密码未配置");
        }
        
        if (!adminPassword.equals(loginDTO.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        
        // 生成 Token
        String token = StringTools.generateToken();
        
        // 保存 Token 到 Redis（使用userId作为标识）
        redisCache.saveToken(token, user.getUserId());

        AdminLoginVO loginVO = new AdminLoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getUserId());
        loginVO.setUsername(user.getUsername());

        log.info("管理员登录成功，userId: {}", user.getUserId());
        return loginVO;
    }

    @Override
    public void logout(String token) {
        Long userId = redisCache.getConsignorIdByToken(token);
        if (userId != null) {
            redisCache.deleteToken(token);
            log.info("管理员登出成功，userId: {}", userId);
        }
    }

    @Override
    public PageResult<UserVO> queryUsers(UserQueryDTO queryDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        queryWrapper.eq(queryDTO.getType() != null, User::getType, queryDTO.getType())
                .like(queryDTO.getUsername() != null && !queryDTO.getUsername().isEmpty(),
                        User::getUsername, queryDTO.getUsername())
                .eq(queryDTO.getStatus() != null, User::getStatus, queryDTO.getStatus())
                .orderByDesc(User::getUserId);

        // 分页查询
        Page<User> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<User> resultPage = userDao.selectPage(page, queryWrapper);

        // 转换为VO
        List<UserVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateDTO createDTO) {
        // 验证用户类型
        if (createDTO.getType() == null || createDTO.getType() < 1 || createDTO.getType() > 3) {
            throw new BusinessException("用户类型错误");
        }

        // 检查用户名是否已存在
        User existUser = userDao.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, createDTO.getUsername()));
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 创建User记录
        User user = new User();
        user.setUsername(createDTO.getUsername());
        user.setType(createDTO.getType());
        user.setStatus(1); // 默认启用
        userDao.insert(user);

        // 根据类型创建对应的角色记录
        String encryptedPassword = DigestUtil.md5Hex(createDTO.getPassword());
        
        if (createDTO.getType() == 1) {
            // 创建货主
            if (createDTO.getEmail() == null || createDTO.getPhone() == null) {
                throw new BusinessException("货主必须提供邮箱和手机号");
            }
            
            // 检查手机号和邮箱是否已存在
            Consignor existConsignor = consignorDao.selectOne(new LambdaQueryWrapper<Consignor>()
                    .eq(Consignor::getPhone, createDTO.getPhone())
                    .or()
                    .eq(Consignor::getEmail, createDTO.getEmail()));
            if (existConsignor != null) {
                userDao.deleteById(user.getUserId());
                throw new BusinessException("该手机号或邮箱已注册");
            }
            
            Consignor consignor = new Consignor();
            consignor.setUserId(user.getUserId());
            consignor.setEmail(createDTO.getEmail());
            consignor.setPhone(createDTO.getPhone());
            consignor.setPassword(encryptedPassword);
            consignorDao.insert(consignor);
            log.info("管理员创建货主成功，userId: {}, consignorId: {}", user.getUserId(), consignor.getConsignorId());
            
        } else if (createDTO.getType() == 2) {
            // 创建调度员
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setUserId(user.getUserId());
            dispatcher.setPassword(encryptedPassword);
            dispatcher.setEmail(createDTO.getEmail());
            dispatcher.setPhone(createDTO.getPhone());
            dispatcher.setStatus(1); // 默认启用
            dispatcher.setCreateTime(new Date());
            dispatcherDao.insert(dispatcher);
            log.info("管理员创建调度员成功，userId: {}, dispatcherId: {}", user.getUserId(), dispatcher.getDispatcherId());
            
        } else if (createDTO.getType() == 3) {
            // 创建司机
            if (createDTO.getEmail() == null || createDTO.getPhone() == null) {
                throw new BusinessException("司机必须提供邮箱和手机号");
            }
            
            // 检查手机号和邮箱是否已存在
            Driver existDriver = driverDao.selectOne(new LambdaQueryWrapper<Driver>()
                    .eq(Driver::getPhone, createDTO.getPhone())
                    .or()
                    .eq(Driver::getEmail, createDTO.getEmail()));
            if (existDriver != null) {
                userDao.deleteById(user.getUserId());
                throw new BusinessException("该手机号或邮箱已注册");
            }
            
            Driver driver = new Driver();
            driver.setUserId(user.getUserId());
            driver.setEmail(createDTO.getEmail());
            driver.setPhone(createDTO.getPhone());
            driver.setPassword(encryptedPassword);
            driverDao.insert(driver);
            log.info("管理员创建司机成功，userId: {}, driverId: {}", user.getUserId(), driver.getDriverId());
        }

        return user.getUserId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long userId, UserUpdateDTO updateDTO) {
        User user = userDao.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新用户名
        if (updateDTO.getUsername() != null && !updateDTO.getUsername().isEmpty()) {
            // 检查新用户名是否已被其他用户使用
            User existUser = userDao.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, updateDTO.getUsername())
                    .ne(User::getUserId, userId));
            if (existUser != null) {
                throw new BusinessException("用户名已被使用");
            }
            user.setUsername(updateDTO.getUsername());
        }

        userDao.updateById(user);

        // 根据用户类型更新对应的角色信息
        if (user.getType() == 1) {
            // 更新货主信息
            Consignor consignor = consignorDao.selectOne(new LambdaQueryWrapper<Consignor>()
                    .eq(Consignor::getUserId, userId));
            if (consignor != null) {
                if (updateDTO.getEmail() != null) {
                    consignor.setEmail(updateDTO.getEmail());
                }
                if (updateDTO.getPhone() != null) {
                    consignor.setPhone(updateDTO.getPhone());
                }
                if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
                    consignor.setPassword(DigestUtil.md5Hex(updateDTO.getPassword()));
                }
                consignorDao.updateById(consignor);
            }
        } else if (user.getType() == 2) {
            // 更新调度员信息
            Dispatcher dispatcher = dispatcherDao.selectOne(new LambdaQueryWrapper<Dispatcher>()
                    .eq(Dispatcher::getUserId, userId));
            if (dispatcher != null) {
                if (updateDTO.getEmail() != null) {
                    dispatcher.setEmail(updateDTO.getEmail());
                }
                if (updateDTO.getPhone() != null) {
                    dispatcher.setPhone(updateDTO.getPhone());
                }
                if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
                    dispatcher.setPassword(DigestUtil.md5Hex(updateDTO.getPassword()));
                }
                dispatcherDao.updateById(dispatcher);
            }
        } else if (user.getType() == 3) {
            // 更新司机信息
            Driver driver = driverDao.selectOne(new LambdaQueryWrapper<Driver>()
                    .eq(Driver::getUserId, userId));
            if (driver != null) {
                if (updateDTO.getEmail() != null) {
                    driver.setEmail(updateDTO.getEmail());
                }
                if (updateDTO.getPhone() != null) {
                    driver.setPhone(updateDTO.getPhone());
                }
                if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
                    driver.setPassword(DigestUtil.md5Hex(updateDTO.getPassword()));
                }
                driverDao.updateById(driver);
            }
        }

        log.info("管理员更新用户信息成功，userId: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        User user = userDao.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getType() == 0) {
            throw new BusinessException("不能删除管理员账户");
        }

        // 删除对应的角色记录
        if (user.getType() == 1) {
            consignorDao.delete(new LambdaQueryWrapper<Consignor>()
                    .eq(Consignor::getUserId, userId));
        } else if (user.getType() == 2) {
            dispatcherDao.delete(new LambdaQueryWrapper<Dispatcher>()
                    .eq(Dispatcher::getUserId, userId));
        } else if (user.getType() == 3) {
            driverDao.delete(new LambdaQueryWrapper<Driver>()
                    .eq(Driver::getUserId, userId));
        }

        // 删除User记录
        userDao.deleteById(userId);

        log.info("管理员删除用户成功，userId: {}", userId);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值错误");
        }

        User user = userDao.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getType() == 0) {
            throw new BusinessException("不能修改管理员账户状态");
        }

        user.setStatus(status);
        userDao.updateById(user);

        log.info("管理员更新用户状态成功，userId: {}, status: {}", userId, status);
    }

    @Override
    public Long getAdminIdByToken(String token) {
        Long userId = redisCache.getConsignorIdByToken(token);
        if (userId == null) {
            return null;
        }
        
        User user = userDao.selectById(userId);
        if (user == null || user.getType() != 0) {
            return null;
        }
        
        return userId;
    }

    /**
     * 将User实体转换为UserVO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setUserId(user.getUserId());
        vo.setType(user.getType());
        vo.setUsername(user.getUsername());
        vo.setStatus(user.getStatus());

        // 设置类型描述
        String[] typeDescs = {"管理员", "货主", "调度员", "司机"};
        if (user.getType() >= 0 && user.getType() < typeDescs.length) {
            vo.setTypeDesc(typeDescs[user.getType()]);
        }

        // 设置状态描述
        vo.setStatusDesc(user.getStatus() == 1 ? "启用" : "禁用");

        // 根据类型查询对应的角色信息
        if (user.getType() == 1) {
            Consignor consignor = consignorDao.selectOne(new LambdaQueryWrapper<Consignor>()
                    .eq(Consignor::getUserId, user.getUserId()));
            if (consignor != null) {
                vo.setEmail(consignor.getEmail());
                vo.setPhone(consignor.getPhone());
                vo.setRoleId(consignor.getConsignorId());
            }
        } else if (user.getType() == 2) {
            Dispatcher dispatcher = dispatcherDao.selectOne(new LambdaQueryWrapper<Dispatcher>()
                    .eq(Dispatcher::getUserId, user.getUserId()));
            if (dispatcher != null) {
                vo.setEmail(dispatcher.getEmail());
                vo.setPhone(dispatcher.getPhone());
                vo.setRoleId(dispatcher.getDispatcherId());
            }
        } else if (user.getType() == 3) {
            Driver driver = driverDao.selectOne(new LambdaQueryWrapper<Driver>()
                    .eq(Driver::getUserId, user.getUserId()));
            if (driver != null) {
                vo.setEmail(driver.getEmail());
                vo.setPhone(driver.getPhone());
                vo.setRoleId(driver.getDriverId());
            }
        }

        return vo;
    }
}

