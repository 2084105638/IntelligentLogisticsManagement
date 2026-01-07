package com.sylphy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sylphy.common.PageResult;
import com.sylphy.common.RedisCache;
import com.sylphy.common.StringTools;
import com.sylphy.common.WaybillStatus;
import com.sylphy.dto.DriverLocationUploadDTO;
import com.sylphy.dto.DriverLoginDTO;
import com.sylphy.dto.DriverRegisterDTO;
import com.sylphy.dto.WaybillQueryDTO;
import com.sylphy.entity.model.Car;
import com.sylphy.entity.model.Driver;
import com.sylphy.entity.model.Location;
import com.sylphy.entity.model.User;
import com.sylphy.entity.model.Waybill;
import com.sylphy.exception.BusinessException;
import com.sylphy.mapper.CarDao;
import com.sylphy.mapper.DriverDao;
import com.sylphy.mapper.LocationDao;
import com.sylphy.mapper.UserDao;
import com.sylphy.mapper.WaybillDao;
import com.sylphy.service.DriverService;
import com.sylphy.vo.DriverLoginVO;
import com.sylphy.vo.WaybillVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    private final CarDao carDao;
    private final WaybillDao waybillDao;
    private final LocationDao locationDao;

    public DriverServiceImpl(DriverDao driverDao, UserDao userDao, RedisCache redisCache, CarDao carDao, WaybillDao waybillDao, LocationDao locationDao) {
        this.driverDao = driverDao;
        this.userDao = userDao;
        this.redisCache = redisCache;
        this.carDao = carDao;
        this.waybillDao = waybillDao;
        this.locationDao = locationDao;
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

    @Override
    public PageResult<WaybillVO> queryWaybills(Long driverId, WaybillQueryDTO queryDTO) {
        // 1. 获取该司机的所有车辆ID
        List<Car> cars = carDao.selectList(new LambdaQueryWrapper<Car>().eq(Car::getDriverId, driverId));
        if (cars == null || cars.isEmpty()) {
            return new PageResult<>(0L, queryDTO.getCurrent(), queryDTO.getSize(), Collections.emptyList());
        }
        List<Long> carIds = cars.stream().map(Car::getCarId).collect(Collectors.toList());

        // 2. 构建运单查询条件
        LambdaQueryWrapper<Waybill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Waybill::getCarId, carIds)
                .eq(queryDTO.getStatus() != null, Waybill::getStatus, queryDTO.getStatus())
                .like(StringUtils.hasText(queryDTO.getStartAddress()), Waybill::getStartAddress, queryDTO.getStartAddress())
                .like(StringUtils.hasText(queryDTO.getEndAddress()), Waybill::getEndAddress, queryDTO.getEndAddress())
                .ge(queryDTO.getStartTime() != null, Waybill::getCreateTime, queryDTO.getStartTime())
                .le(queryDTO.getEndTime() != null, Waybill::getCreateTime, queryDTO.getEndTime());

        queryWrapper.orderByDesc(Waybill::getCreateTime);

        // 3. 执行分页查询
        Page<Waybill> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<Waybill> resultPage = waybillDao.selectPage(page, queryWrapper);

        // 4. 转换 VO
        List<WaybillVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadLocation(Long driverId, DriverLocationUploadDTO dto) {
        // 1. 更新车辆位置
        Car car = carDao.selectOne(new LambdaQueryWrapper<Car>().eq(Car::getDriverId, driverId));
        if (car == null) {
            throw new BusinessException("未找到该司机驾驶的车辆");
        }
        car.setLocation(dto.getLocation());
        carDao.updateById(car);

        // 2. 如果有运单，记录运单轨迹
        if (dto.getWaybillId() != null) {
            // 可选：校验运单是否存在且状态正确
            Waybill waybill = waybillDao.selectById(dto.getWaybillId());
            if (waybill != null) {
                // 插入历史轨迹
                Location location = new Location();
                location.setWaybillId(dto.getWaybillId());
                location.setLocationInfo(dto.getLocation());
                location.setLocationDate(new Date());
                locationDao.insert(location);
            }
        }
    }

    private WaybillVO convertToVO(Waybill waybill) {
        WaybillVO vo = BeanUtil.copyProperties(waybill, WaybillVO.class);
        vo.setStatusDesc(WaybillStatus.getDesc(waybill.getStatus()));
        return vo;
    }
}
