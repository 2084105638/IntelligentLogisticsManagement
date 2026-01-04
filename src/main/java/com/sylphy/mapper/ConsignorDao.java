package com.sylphy.mapper;

import com.sylphy.entity.model.Consignor;
import org.apache.ibatis.annotations.Param;

public interface ConsignorDao {
    int deleteByPrimaryKey(Long consignorId);

    int insert(Consignor record);

    int insertSelective(Consignor record);

    Consignor selectByPrimaryKey(Long consignorId);

    int updateByPrimaryKeySelective(Consignor record);

    int updateByPrimaryKey(Consignor record);

    /**
     * 根据手机号查询货主
     */
    Consignor selectByPhone(@Param("phone") String phone);

    /**
     * 根据邮箱查询货主
     */
    Consignor selectByEmail(@Param("email") String email);

    /**
     * 根据账号（手机号或邮箱）查询货主
     */
    Consignor selectByAccount(@Param("account") String account);
}