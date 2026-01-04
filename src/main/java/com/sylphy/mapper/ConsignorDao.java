package com.sylphy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sylphy.entity.model.Consignor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ConsignorDao extends BaseMapper<Consignor> {
}