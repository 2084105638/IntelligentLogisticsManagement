package com.sylphy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sylphy.entity.model.WaybillException;
import org.apache.ibatis.annotations.Mapper;

/**
 * 运单异常 DAO
 */
@Mapper
public interface WaybillExceptionDao extends BaseMapper<WaybillException> {
}
