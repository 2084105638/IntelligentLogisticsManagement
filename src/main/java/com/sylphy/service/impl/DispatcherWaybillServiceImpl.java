package com.sylphy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sylphy.common.PageResult;
import com.sylphy.common.WaybillStatus;
import com.sylphy.dto.DispatcherWaybillQueryDTO;
import com.sylphy.entity.model.Waybill;
import com.sylphy.exception.BusinessException;
import com.sylphy.mapper.WaybillDao;
import com.sylphy.service.DispatcherWaybillService;
import com.sylphy.vo.WaybillVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DispatcherWaybillServiceImpl implements DispatcherWaybillService {

    private final WaybillDao waybillDao;

    public DispatcherWaybillServiceImpl(WaybillDao waybillDao) {
        this.waybillDao = waybillDao;
    }

    @Override
    public PageResult<WaybillVO> queryWaybillsForDispatcher(DispatcherWaybillQueryDTO queryDTO) {
        LambdaQueryWrapper<Waybill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(queryDTO.getStatus() != null, Waybill::getStatus, queryDTO.getStatus())
                .like(StringUtils.hasText(queryDTO.getStartAddress()), Waybill::getStartAddress, queryDTO.getStartAddress())
                .like(StringUtils.hasText(queryDTO.getEndAddress()), Waybill::getEndAddress, queryDTO.getEndAddress())
                .orderByDesc(Waybill::getCreateTime);

        Page<Waybill> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<Waybill> resultPage = waybillDao.selectPage(page, queryWrapper);

        List<WaybillVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize(), voList);
    }

    @Override
    public WaybillVO getWaybillDetailForDispatcher(Long waybillId) {
        Waybill waybill = waybillDao.selectById(waybillId);
        if (waybill == null) {
            throw new BusinessException("运单不存在");
        }
        return convertToVO(waybill);
    }

    private WaybillVO convertToVO(Waybill waybill) {
        WaybillVO vo = BeanUtil.copyProperties(waybill, WaybillVO.class);
        vo.setStatusDesc(WaybillStatus.getDesc(waybill.getStatus()));
        return vo;
    }
}
