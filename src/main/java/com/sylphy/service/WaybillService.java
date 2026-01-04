package com.sylphy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sylphy.common.PageResult;
import com.sylphy.dto.WaybillCreateDTO;
import com.sylphy.dto.WaybillQueryDTO;
import com.sylphy.entity.model.Waybill;
import com.sylphy.vo.WaybillVO;

/**
 * 运单 Service 接口
 *
 * @author apple
 * @since 2026/1/4
 */
public interface WaybillService extends IService<Waybill> {

    /**
     * 发布运单
     *
     * @param consignorId 货主ID
     * @param createDTO   运单创建信息
     * @return 运单ID
     */
    Long createWaybill(Long consignorId, WaybillCreateDTO createDTO);

    /**
     * 查询运单详情
     *
     * @param waybillId 运单ID
     * @return 运单详情
     */
    WaybillVO getWaybillDetail(Long waybillId);

    /**
     * 分页查询货主的历史运单
     *
     * @param consignorId 货主ID
     * @param queryDTO    查询条件
     * @return 分页结果
     */
    PageResult<WaybillVO> queryConsignorWaybills(Long consignorId, WaybillQueryDTO queryDTO);

    /**
     * 取消运单（仅待分配状态可取消）
     *
     * @param waybillId   运单ID
     * @param consignorId 货主ID
     */
    void cancelWaybill(Long waybillId, Long consignorId);
}
