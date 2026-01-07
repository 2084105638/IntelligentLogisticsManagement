package com.sylphy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sylphy.common.PageResult;
import com.sylphy.common.WaybillStatus;
import com.sylphy.entity.model.Car;
import com.sylphy.entity.model.Waybill;
import com.sylphy.entity.model.WaybillAssignment;
import com.sylphy.exception.BusinessException;
import com.sylphy.mapper.CarDao;
import com.sylphy.mapper.WaybillAssignmentDao;
import com.sylphy.mapper.WaybillDao;
import com.sylphy.service.AssignmentService;
import com.sylphy.vo.WaybillAssignmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final WaybillDao waybillDao;
    private final CarDao carDao;
    private final WaybillAssignmentDao assignmentDao;

    public AssignmentServiceImpl(WaybillDao waybillDao, CarDao carDao, WaybillAssignmentDao assignmentDao) {
        this.waybillDao = waybillDao;
        this.carDao = carDao;
        this.assignmentDao = assignmentDao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long assignVehicle(Long oldWaybillId, Long carId, Long operatorId) {
        Waybill old = waybillDao.selectById(oldWaybillId);
        if (old == null) {
            throw new BusinessException("运单不存在");
        }
        if (!WaybillStatus.PENDING_ALLOCATION.getCode().equals(old.getStatus())) {
            throw new BusinessException("仅待分配状态允许分配车辆");
        }
        Car car = carDao.selectById(carId);
        if (car == null || car.getStatus() == null || car.getStatus() != 0) {
            throw new BusinessException("车辆不可用");
        }

        Waybill nw = copyWaybill(old);
        nw.setCarId(carId);
        nw.setStatus(WaybillStatus.ALLOCATED.getCode());
        nw.setWaybillIdentification(old.getWaybillIdentification());
        nw.setChanged(0);
        waybillDao.insert(nw);

        old.setChanged(1);
        waybillDao.updateById(old);

        car.setStatus(1);
        carDao.updateById(car);

        WaybillAssignment assignment = new WaybillAssignment();
        assignment.setOperatorId(operatorId);
        assignment.setOldWaybillId(old.getWaybillId());
        assignment.setNewWaybillId(nw.getWaybillId());
        assignment.setCreateTime(new Date());
        assignmentDao.insert(assignment);

        return nw.getWaybillId();
    }

    @Override
    public Long autoMatchAndAssign(Long oldWaybillId, Long operatorId) {
        Car car = carDao.selectOne(new LambdaQueryWrapper<Car>().eq(Car::getStatus, 0).last("limit 1"));
        if (car == null) {
            throw new BusinessException("无可用车辆");
        }
        return assignVehicle(oldWaybillId, car.getCarId(), operatorId);
    }

    @Override
    public Long reassignVehicle(Long oldWaybillId, Long newCarId, Long operatorId) {
        Waybill old = waybillDao.selectById(oldWaybillId);
        if (old == null) {
            throw new BusinessException("运单不存在");
        }
        Car newCar = carDao.selectById(newCarId);
        if (newCar == null || newCar.getStatus() == null || newCar.getStatus() != 0) {
            throw new BusinessException("新车辆不可用");
        }

        Long newWaybillId = assignVehicle(oldWaybillId, newCarId, operatorId);
        Long oldCarId = old.getCarId();
        if (oldCarId != null) {
            Car oldCar = carDao.selectById(oldCarId);
            if (oldCar != null) {
                oldCar.setStatus(0);
                carDao.updateById(oldCar);
            }
        }
        return newWaybillId;
    }

    @Override
    public Long changeStatus(Long oldWaybillId, Integer changeStatus, Long operatorId) {
        Waybill old = waybillDao.selectById(oldWaybillId);
        if (old == null) {
            throw new BusinessException("运单不存在");
        }
        if (old.getChanged() != null && old.getChanged() == 1) {
            throw new BusinessException("该运单已被修改，请刷新后重试");
        }
        // 简单校验状态流转：只允许向后流转，或者根据业务规则定义
        if (changeStatus < 0 || changeStatus > 3) {
            throw new BusinessException("无效的状态");
        }
        if (old.getStatus() >= changeStatus) {
            throw new BusinessException("状态不可回退或重复设置");
        }

        // 复制新运单
        Waybill nw = copyWaybill(old);
        nw.setStatus(changeStatus);
        // 如果是已完成状态，可能需要释放车辆，此处暂保持 carId 不变
        // 实际上 copyWaybill 没有复制 carId 和 waybillIdentification，需手动补全
        nw.setCarId(old.getCarId());
        nw.setWaybillIdentification(old.getWaybillIdentification());
        nw.setChanged(0);
        waybillDao.insert(nw);

        // 标记旧运单
        old.setChanged(1);
        waybillDao.updateById(old);

        // 记录历史
        WaybillAssignment assignment = new WaybillAssignment();
        assignment.setOperatorId(operatorId);
        assignment.setOldWaybillId(old.getWaybillId());
        assignment.setNewWaybillId(nw.getWaybillId());
        assignment.setCreateTime(new Date());
        assignmentDao.insert(assignment);

        return nw.getWaybillId();
    }

    @Override
    public PageResult<WaybillAssignmentVO> listAssignments(Long oldWaybillId, Long newWaybillId, Long operatorId, long current, long size) {
        LambdaQueryWrapper<WaybillAssignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(oldWaybillId != null, WaybillAssignment::getOldWaybillId, oldWaybillId)
                .eq(newWaybillId != null, WaybillAssignment::getNewWaybillId, newWaybillId)
                .eq(operatorId != null, WaybillAssignment::getOperatorId, operatorId)
                .orderByDesc(WaybillAssignment::getCreateTime);
        Page<WaybillAssignment> page = new Page<>(current, size);
        Page<WaybillAssignment> rp = assignmentDao.selectPage(page, wrapper);
        List<WaybillAssignmentVO> list = rp.getRecords().stream()
                .map(a -> BeanUtil.copyProperties(a, WaybillAssignmentVO.class))
                .collect(Collectors.toList());
        return new PageResult<>(rp.getTotal(), rp.getCurrent(), rp.getSize(), list);
    }

    private Waybill copyWaybill(Waybill old) {
        Waybill nw = new Waybill();
        nw.setGoodsInformation(old.getGoodsInformation());
        nw.setStartAddress(old.getStartAddress());
        nw.setEndAddress(old.getEndAddress());
        nw.setCreateTime(new Date());
        nw.setExpectedTimeLimit(old.getExpectedTimeLimit());
        nw.setCost(old.getCost());
        nw.setStatus(old.getStatus());
        nw.setCreatedConsignor(old.getCreatedConsignor());
        nw.setReceivingConsignor(old.getReceivingConsignor());
        nw.setChanged(0);
        return nw;
    }
}
