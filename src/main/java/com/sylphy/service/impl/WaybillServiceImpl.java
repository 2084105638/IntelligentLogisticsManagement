package com.sylphy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sylphy.common.PageResult;
import com.sylphy.common.WaybillStatus;
import com.sylphy.dto.WaybillCreateDTO;
import com.sylphy.dto.WaybillQueryDTO;
import com.sylphy.entity.model.Consignor;
import com.sylphy.entity.model.Waybill;
import com.sylphy.exception.BusinessException;
import com.sylphy.mapper.ConsignorDao;
import com.sylphy.mapper.WaybillDao;
import com.sylphy.service.ConsignorService;
import com.sylphy.service.WaybillService;
import com.sylphy.vo.WaybillVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 运单 Service 实现类
 *
 * @author apple
 * @since 2026/1/4
 */
@Slf4j
@Service
public class WaybillServiceImpl extends ServiceImpl<WaybillDao, Waybill> implements WaybillService {
    
    private final ConsignorService consignorService;
    private final ConsignorDao consignorDao;
    
    public WaybillServiceImpl(ConsignorService consignorService, ConsignorDao consignorDao) {
        this.consignorService = consignorService;
        this.consignorDao = consignorDao;
    }
    
    @Override
    public Long createWaybill(Long consignorId, WaybillCreateDTO createDTO) {
        // 验证期望时效不能早于当前时间
        if (createDTO.getExpectedTimeLimit().before(new Date())) {
            throw new BusinessException("期望时效不能早于当前时间");
        }
        //查询收货者
        Consignor consignor = consignorDao.selectById(createDTO.getReceivingConsignorId());
        if (consignor == null) {
            throw new BusinessException("收货人不存在");
        }
        
        // 创建运单实体
        Waybill waybill = new Waybill();
        waybill.setCreatedConsignor(consignorId);
        waybill.setReceivingConsignor(createDTO.getReceivingConsignorId());
        waybill.setGoodsInformation(createDTO.getGoodsInformation());
        waybill.setStartAddress(createDTO.getStartAddress());
        waybill.setEndAddress(createDTO.getEndAddress());
        waybill.setExpectedTimeLimit(createDTO.getExpectedTimeLimit());
        waybill.setCost(createDTO.getCost());
        waybill.setStatus(WaybillStatus.PENDING_ALLOCATION.getCode());
        waybill.setCreateTime(new Date());
        
        // 保存运单
        boolean success = save(waybill);
        if (!success) {
            throw new BusinessException("创建运单失败");
        }
        
        log.info("货主 {} 创建运单成功，运单ID: {}，收货者ID: {}",
                consignorId, waybill.getWaybillId(), createDTO.getReceivingConsignorId());
        return waybill.getWaybillId();
    }
    
    @Override
    public WaybillVO getWaybillDetail(Long waybillId, Long id) {
        Waybill waybill = getById(waybillId);
        if (waybill == null) {
            throw new BusinessException("运单不存在");
        }
        if (!waybill.getCreatedConsignor().equals(id) && !waybill.getReceivingConsignor().equals(id)) {
            throw new BusinessException("参数错误");
        }
        
        return convertToVO(waybill);
    }
    
    @Override
    public PageResult<WaybillVO> queryConsignorWaybills(Long consignorId, WaybillQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<Waybill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(w -> w
                                      .eq(Waybill::getCreatedConsignor, consignorId)
                                      .or()
                                      .eq(Waybill::getReceivingConsignor, consignorId)
                )
                .eq(queryDTO.getStatus() != null, Waybill::getStatus, queryDTO.getStatus())
                .like(StringUtils.hasText(queryDTO.getStartAddress()),
                        Waybill::getStartAddress, queryDTO.getStartAddress())
                .like(StringUtils.hasText(queryDTO.getEndAddress()),
                        Waybill::getEndAddress, queryDTO.getEndAddress())
                .orderByDesc(Waybill::getCreateTime);
        
        // 分页查询
        Page<Waybill> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<Waybill> resultPage = page(page, queryWrapper);
        
        // 转换为 VO
        List<WaybillVO> voList = resultPage.getRecords().stream()
                                         .map(this::convertToVO)
                                         .collect(Collectors.toList());
        
        return new PageResult<>(
                resultPage.getTotal(),
                resultPage.getCurrent(),
                resultPage.getSize(),
                voList
        );
    }
    
    @Override
    public void cancelWaybill(Long waybillId, Long consignorId) {
        Waybill waybill = getById(waybillId);
        if (waybill == null) {
            throw new BusinessException("运单不存在");
        }
        
        // 验证是否是该货主创建的运单
        if (!waybill.getCreatedConsignor().equals(consignorId)) {
            throw new BusinessException("无权操作此运单");
        }
        
        // 只有待分配状态的运单可以取消
        if (!WaybillStatus.PENDING_ALLOCATION.getCode().equals(waybill.getStatus())) {
            throw new BusinessException("只有待分配状态的运单可以取消");
        }
        
        // 删除运单
        boolean success = removeById(waybillId);
        if (!success) {
            throw new BusinessException("取消运单失败");
        }
        
        log.info("货主 {} 取消运单成功，运单ID: {}", consignorId, waybillId);
    }
    
    /**
     * 转换为 VO
     */
    private WaybillVO convertToVO(Waybill waybill) {
        WaybillVO vo = BeanUtil.copyProperties(waybill, WaybillVO.class);
        vo.setStatusDesc(WaybillStatus.getDesc(waybill.getStatus()));
        return vo;
    }
}
