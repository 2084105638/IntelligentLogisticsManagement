package com.sylphy.common;

import com.sylphy.entity.model.Consignor;
import com.sylphy.entity.model.Dispatcher;
import com.sylphy.entity.model.Driver;
import org.springframework.stereotype.Component;

/**
 * Redis 业务操作类
 *
 * @author apple
 * @since 2026/1/3
 */
@Component
public class RedisCache {

    private final RedisUtil redisUtil;

    public RedisCache(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    /**
     * 保存 Token
     *
     * @param token       token值
     * @param consignorId 货主ID
     */
    public void saveToken(String token, Long consignorId) {
        String key = RedisConstants.TOKEN_PREFIX + token;
        redisUtil.set(key, consignorId, RedisConstants.TOKEN_EXPIRE_TIME);
    }

    /**
     * 获取 Token 对应的货主ID
     *
     * @param token token值
     * @return 货主ID
     */
    public Long getConsignorIdByToken(String token) {
        String key = RedisConstants.TOKEN_PREFIX + token;
        Object value = redisUtil.get(key);
        return value != null ? Long.valueOf(value.toString()) : null;
    }

    /**
     * 获取 Token 对应的调度员ID
     *
     * @param token token值
     * @return 调度员ID
     */
    public Long getDispatcherIdByToken(String token) {
        String key = RedisConstants.TOKEN_PREFIX + token;
        Object value = redisUtil.get(key);
        return value != null ? Long.valueOf(value.toString()) : null;
    }

    /**
     * 获取 Token 对应的司机ID
     *
     * @param token token值
     * @return 司机ID
     */
    public Long getDriverIdByToken(String token) {
        String key = RedisConstants.TOKEN_PREFIX + token;
        Object value = redisUtil.get(key);
        return value != null ? Long.valueOf(value.toString()) : null;
    }

    /**
     * 删除 Token
     *
     * @param token token值
     */
    public void deleteToken(String token) {
        String key = RedisConstants.TOKEN_PREFIX + token;
        redisUtil.del(key);
    }

    /**
     * 刷新 Token 过期时间
     *
     * @param token token值
     */
    public void refreshToken(String token) {
        String key = RedisConstants.TOKEN_PREFIX + token;
        redisUtil.expire(key, RedisConstants.TOKEN_EXPIRE_TIME);
    }

    /**
     * 保存货主信息到缓存
     *
     * @param consignor 货主信息
     */
    public void saveConsignorInfo(Consignor consignor) {
        String key = RedisConstants.CONSIGNOR_INFO_PREFIX + consignor.getConsignorId();
        redisUtil.set(key, consignor, RedisConstants.USER_INFO_EXPIRE_TIME);
    }

    /**
     * 从缓存获取货主信息
     *
     * @param consignorId 货主ID
     * @return 货主信息
     */
    public Consignor getConsignorInfo(Long consignorId) {
        String key = RedisConstants.CONSIGNOR_INFO_PREFIX + consignorId;
        Object value = redisUtil.get(key);
        return value != null ? (Consignor) value : null;
    }

    /**
     * 删除货主信息缓存
     *
     * @param consignorId 货主ID
     */
    public void deleteConsignorInfo(Long consignorId) {
        String key = RedisConstants.CONSIGNOR_INFO_PREFIX + consignorId;
        redisUtil.del(key);
    }

    /**
     * 刷新货主信息缓存过期时间
     *
     * @param consignorId 货主ID
     */
    public void refreshConsignorInfo(Long consignorId) {
        String key = RedisConstants.CONSIGNOR_INFO_PREFIX + consignorId;
        redisUtil.expire(key, RedisConstants.USER_INFO_EXPIRE_TIME);
    }

    /**
     * 保存调度员信息到缓存
     *
     * @param dispatcher 调度员信息
     */
    public void saveDispatcherInfo(Dispatcher dispatcher) {
        String key = RedisConstants.DISPATCHER_INFO_PREFIX + dispatcher.getDispatcherId();
        redisUtil.set(key, dispatcher, RedisConstants.USER_INFO_EXPIRE_TIME);
    }

    /**
     * 从缓存获取调度员信息
     *
     * @param dispatcherId 调度员ID
     * @return 调度员信息
     */
    public Dispatcher getDispatcherInfo(Long dispatcherId) {
        String key = RedisConstants.DISPATCHER_INFO_PREFIX + dispatcherId;
        Object value = redisUtil.get(key);
        return value != null ? (Dispatcher) value : null;
    }

    /**
     * 删除调度员信息缓存
     *
     * @param dispatcherId 调度员ID
     */
    public void deleteDispatcherInfo(Long dispatcherId) {
        String key = RedisConstants.DISPATCHER_INFO_PREFIX + dispatcherId;
        redisUtil.del(key);
    }

    /**
     * 刷新调度员信息缓存过期时间
     *
     * @param dispatcherId 调度员ID
     */
    public void refreshDispatcherInfo(Long dispatcherId) {
        String key = RedisConstants.DISPATCHER_INFO_PREFIX + dispatcherId;
        redisUtil.expire(key, RedisConstants.USER_INFO_EXPIRE_TIME);
    }

    /**
     * 保存司机信息到缓存
     *
     * @param driver 司机信息
     */
    public void saveDriverInfo(Driver driver) {
        String key = RedisConstants.DRIVER_INFO_PREFIX + driver.getDriverId();
        redisUtil.set(key, driver, RedisConstants.USER_INFO_EXPIRE_TIME);
    }

    /**
     * 从缓存获取司机信息
     *
     * @param driverId 司机ID
     * @return 司机信息
     */
    public Driver getDriverInfo(Long driverId) {
        String key = RedisConstants.DRIVER_INFO_PREFIX + driverId;
        Object value = redisUtil.get(key);
        return value != null ? (Driver) value : null;
    }

    /**
     * 删除司机信息缓存
     *
     * @param driverId 司机ID
     */
    public void deleteDriverInfo(Long driverId) {
        String key = RedisConstants.DRIVER_INFO_PREFIX + driverId;
        redisUtil.del(key);
    }

    /**
     * 刷新司机信息缓存过期时间
     *
     * @param driverId 司机ID
     */
    public void refreshDriverInfo(Long driverId) {
        String key = RedisConstants.DRIVER_INFO_PREFIX + driverId;
        redisUtil.expire(key, RedisConstants.USER_INFO_EXPIRE_TIME);
    }
}
