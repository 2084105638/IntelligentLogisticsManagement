package com.sylphy.service;

import com.sylphy.dto.ConsignorChangeInfoDTO;
import com.sylphy.dto.ConsignorLoginDTO;
import com.sylphy.dto.ConsignorRegisterDTO;
import com.sylphy.entity.model.Consignor;
import com.sylphy.vo.ConsignorLoginVO;

import java.util.List;

/**
 * 货主 Service 接口
 *
 * @author apple
 * @since 2026/1/3
 */
public interface ConsignorService {

    /**
     * 货主注册
     *
     * @param registerDTO 注册信息
     */
    void register(ConsignorRegisterDTO registerDTO);

    /**
     * 货主登录
     *
     * @param loginDTO 登录信息
     * @return 登录响应
     */
    ConsignorLoginVO login(ConsignorLoginDTO loginDTO);

    /**
     * 货主登出
     *
     * @param token Token
     */
    void logout(String token);

    /**
     * 根据 Token 获取货主信息
     *
     * @param token Token
     * @return 货主信息
     */
    Consignor getConsignorByToken(String token);

    /**
     * 修改货主信息
     *
     * @param consignorId 货主ID
     * @param changeInfoDTO 修改信息
     * @return 更新后的货主信息
     */
    Consignor updateConsignor(Long consignorId, ConsignorChangeInfoDTO changeInfoDTO);

    /**
     * 获取所有货主列表
     *
     * @return 货主列表
     */
    List<Consignor> listAllConsignors();
}
