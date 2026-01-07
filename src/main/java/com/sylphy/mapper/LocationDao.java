package com.sylphy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sylphy.entity.model.Location;
import org.apache.ibatis.annotations.Mapper;

/**
 * 运单历史位置 DAO
 */
@Mapper
public interface LocationDao extends BaseMapper<Location> {
}
