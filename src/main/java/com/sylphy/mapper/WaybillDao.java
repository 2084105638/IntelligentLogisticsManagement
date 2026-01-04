package com.sylphy.mapper;

import generate.Waybill;

public interface WaybillDao {
    int deleteByPrimaryKey(Long waybillId);

    int insert(Waybill record);

    int insertSelective(Waybill record);

    Waybill selectByPrimaryKey(Long waybillId);

    int updateByPrimaryKeySelective(Waybill record);

    int updateByPrimaryKey(Waybill record);
}