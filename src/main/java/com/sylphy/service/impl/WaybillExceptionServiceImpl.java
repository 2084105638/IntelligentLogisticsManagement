package com.sylphy.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sylphy.common.WaybillStatus;
import com.sylphy.dto.WaybillExceptionReportDTO;
import com.sylphy.entity.model.Car;
import com.sylphy.entity.model.Waybill;
import com.sylphy.entity.model.WaybillException;
import com.sylphy.exception.BusinessException;
import com.sylphy.mapper.CarDao;
import com.sylphy.mapper.WaybillDao;
import com.sylphy.mapper.WaybillExceptionDao;
import com.sylphy.service.WaybillExceptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 运单异常 Service 实现类
 */
@Slf4j
@Service
public class WaybillExceptionServiceImpl implements WaybillExceptionService {

    private final WaybillExceptionDao exceptionDao;
    private final WaybillDao waybillDao;
    private final CarDao carDao;

    public WaybillExceptionServiceImpl(WaybillExceptionDao exceptionDao, WaybillDao waybillDao, CarDao carDao) {
        this.exceptionDao = exceptionDao;
        this.waybillDao = waybillDao;
        this.carDao = carDao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportException(Long driverId, WaybillExceptionReportDTO dto) {
        Car car = carDao.selectOne(Wrappers.lambdaQuery(Car.class)
                .eq(Car::getDriverId, driverId));
        // 1. 校验运单是否存在
        Waybill waybill = waybillDao.selectOne(Wrappers.lambdaQuery(Waybill.class)
                .eq(Waybill::getWaybillIdentification, dto.getWaybillIdentification())
                .eq(Waybill::getChanged, 0));
        if (waybill == null) {
            throw new BusinessException("运单不存在");
        }

        // 2. 校验运单是否已结束（可选逻辑，视业务需求而定，通常已完成运单不应再报异常，或者只允许在运输中报）
        // 此处暂不做严格状态校验，仅记录
        if (waybill.getStatus().equals(WaybillStatus.PENDING_ALLOCATION.getCode()) || waybill.getStatus().equals(WaybillStatus.COMPLETED.getCode())) {
            throw new BusinessException("运单未开始或已结束");
        }
        // 3. 构建异常实体
        WaybillException exception = new WaybillException();
        exception.setWaybillIdentification(dto.getWaybillIdentification());
        exception.setCarId(car.getCarId());
        exception.setDescription(dto.getDescription());
        exception.setExceptionDate(dto.getExceptionDate());

        // 4. 保存
        exceptionDao.insert(exception);

        log.info("司机 {} 上报运单 {} 异常: {}", driverId, dto.getWaybillIdentification(), dto.getDescription());
    }
}
