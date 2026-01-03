package com.sylphy.mapper;

import com.sylphy.entity.dao.Consignor;

public interface ConsignorDao {
    int deleteByPrimaryKey(Long consignorId);

    int insert(Consignor record);

    int insertSelective(Consignor record);

    Consignor selectByPrimaryKey(Long consignorId);

    int updateByPrimaryKeySelective(Consignor record);

    int updateByPrimaryKey(Consignor record);
}