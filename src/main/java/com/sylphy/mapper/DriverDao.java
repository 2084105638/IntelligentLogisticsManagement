package com.sylphy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sylphy.entity.model.Driver;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DriverDao extends BaseMapper<Driver> {
}
