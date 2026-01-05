package com.sylphy.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sylphy.common.RedisCache;
import com.sylphy.common.StringTools;
import com.sylphy.dto.DriverLoginDTO;
import com.sylphy.dto.DriverRegisterDTO;
import com.sylphy.entity.model.Driver;
import com.sylphy.entity.model.User;
import com.sylphy.exception.BusinessException;
import com.sylphy.mapper.DriverDao;
import com.sylphy.mapper.UserDao;
import com.sylphy.service.DriverService;
import com.sylphy.vo.DriverLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author apple
 * @since 2026/1/5 08:39
 */
@Slf4j
@Service
public class DriverServiceImpl implements DriverService {

    private final DriverDao driverDao;
    private final UserDao userDao;
    private final RedisCache redisCache;

    public DriverServiceImpl(DriverDao driverDao, UserDao userDao, RedisCache redisCache) {
        this.driverDao = driverDao;
        this.userDao = userDao;
        this.redisCache = redisCache;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(DriverRegisterDTO registerDTO) {
        // 检查手机号
        Driver existDriver = driverDao.selectOne(new LambdaQueryWrapper<Driver>().eq(Driver::getPhone, registerDTO.getPhone()));
        if (existDriver != null) {
            throw new BusinessException("该手机号已注册");
        }
        // 检查邮箱
        existDriver = driverDao.selectOne(new LambdaQueryWrapper<Driver>().eq(Driver::getEmail, registerDTO.getEmail()));
        if (existDriver != null) {
            throw new BusinessException("该邮箱已注册");
        }

        // 创建用户 type=3 (司机)
        User user = new User();
        user.setUsername(StringTools.randomUsername());
        user.setType(3);
        userDao.insert(user);

        // 创建司机
        Driver driver = new Driver();
        driver.setUserId(user.getUserId());
        driver.setEmail(registerDTO.getEmail());
        driver.setPhone(registerDTO.getPhone());
        driver.setPassword(DigestUtil.md5Hex(registerDTO.getPassword()));
        driverDao.insert(driver);

        log.info("司机注册成功，driverId: {}", driver.getDriverId());
    }

    @Override
    public DriverLoginVO login(DriverLoginDTO loginDTO) {
        Driver driver = driverDao.selectOne(new LambdaQueryWrapper<Driver>()
                .eq(Driver::getPhone, loginDTO.getAccount())
                .or()
                .eq(Driver::getEmail, loginDTO.getAccount()));

        if (driver == null) {
            throw new BusinessException("账号或密码错误");
        }

        String encryptedPassword = DigestUtil.md5Hex(loginDTO.getPassword());
        if (!encryptedPassword.equals(driver.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }

        String token = StringTools.generateToken();
        redisCache.saveToken(token, driver.getDriverId());
        redisCache.saveDriverInfo(driver);

        DriverLoginVO vo = new DriverLoginVO();
        vo.setToken(token);
        vo.setDriverId(driver.getDriverId());
        vo.setUserId(driver.getUserId());
        vo.setEmail(driver.getEmail());
        vo.setPhone(driver.getPhone());

        log.info("司机登录成功，driverId: {}", driver.getDriverId());
        return vo;
    }
}
