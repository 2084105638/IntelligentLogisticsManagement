package com.sylphy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sylphy.entity.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao extends BaseMapper<User> {
    // 删除重写的方法，直接使用 BaseMapper 提供的方法
    // BaseMapper 已经提供了 insert, selectById, updateById, deleteById 等方法
}