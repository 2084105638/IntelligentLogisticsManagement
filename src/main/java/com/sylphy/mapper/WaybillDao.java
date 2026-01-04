package com.sylphy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sylphy.entity.model.Waybill;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WaybillDao extends BaseMapper<Waybill> {
    int deleteByPrimaryKey(Long waybillId);

    @Override
    int insert(Waybill record);

    int insertSelective(Waybill record);

    Waybill selectByPrimaryKey(Long waybillId);

    int updateByPrimaryKeySelective(Waybill record);

    int updateByPrimaryKey(Waybill record);
}