package com.sylphy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sylphy.common.PageResult;
import com.sylphy.common.StringTools;
import com.sylphy.dto.CarCreateDTO;
import com.sylphy.dto.CarQueryDTO;
import com.sylphy.dto.CarUpdateDTO;
import com.sylphy.entity.model.Car;
import com.sylphy.exception.BusinessException;
import com.sylphy.mapper.CarDao;
import com.sylphy.service.CarService;
import com.sylphy.vo.CarVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CarServiceImpl implements CarService {
    
    private final CarDao carDao;
    
    public CarServiceImpl(CarDao carDao) {
        this.carDao = carDao;
    }
    
    @Override
    public Long createCar(CarCreateDTO dto) {
        Car car = new Car();
        car.setLocation(dto.getLocation());
        car.setStatus(dto.getStatus() == null ? 0 : dto.getStatus());
        car.setDriverId(dto.getDriverId());
        car.setType(dto.getType());
        int rows = carDao.insert(car);
        if (rows != 1) {
            throw new BusinessException("创建车辆失败");
        }
        return car.getCarId();
    }
    
    @Override
    public void updateCar(CarUpdateDTO dto) {
        Car car = carDao.selectById(dto.getCarId());
        if (car == null) {
            throw new BusinessException("车辆不存在");
        }
        if (dto.getDriverId() != null) {
            car.setDriverId(dto.getDriverId());
        }
        if (StringUtils.hasText(dto.getLocation())) {
            car.setLocation(dto.getLocation());
        }
        if (dto.getStatus() != null) {
            car.setStatus(dto.getStatus());
        }
        if (dto.getType() != null) {
            car.setType(dto.getType());
        }
        if (dto.getType() != null){
            car.setType(dto.getType());
        }
        carDao.updateById(car);
    }
    
    @Override
    public void deleteCar(Long carId) {
        int rows = carDao.deleteById(carId);
        if (rows != 1) {
            throw new BusinessException("删除车辆失败");
        }
    }
    
    @Override
    public PageResult<CarVO> queryCars(CarQueryDTO dto) {
        LambdaQueryWrapper<Car> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getStatus() != null, Car::getStatus, dto.getStatus())
                .eq(dto.getDriverId() != null, Car::getDriverId, dto.getDriverId())
                .like(StringUtils.hasText(dto.getLocation()), Car::getLocation, dto.getLocation());
        
        Page<Car> page = new Page<>(dto.getCurrent(), dto.getSize());
        Page<Car> rp = carDao.selectPage(page, wrapper);
        List<CarVO> list = rp.getRecords().stream()
                                   .map(c -> BeanUtil.copyProperties(c, CarVO.class))
                                   .collect(Collectors.toList());
        return new PageResult<>(rp.getTotal(), rp.getCurrent(), rp.getSize(), list);
    }
    
    @Override
    public Integer getStatus(Long carId) {
        Car car = carDao.selectById(carId);
        if (car == null) {
            throw new BusinessException("车辆不存在");
        }
        return car.getStatus();
    }
    
    @Override
    public void updateStatus(Long carId, Integer status) {
        Car car = carDao.selectById(carId);
        if (car == null) {
            throw new BusinessException("车辆不存在");
        }
        car.setStatus(status);
        carDao.updateById(car);
    }
    
    @Override
    public String getLocation(Long carId) {
        Car car = carDao.selectById(carId);
        if (car == null) {
            throw new BusinessException("车辆不存在");
        }
        return car.getLocation();
    }
    
    @Override
    public void updateLocation(Long carId, String location) {
        Car car = carDao.selectById(carId);
        if (car == null) {
            throw new BusinessException("车辆不存在");
        }
        car.setLocation(location);
        carDao.updateById(car);
    }
}
