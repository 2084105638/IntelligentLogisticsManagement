package com.sylphy.common;

import com.sylphy.entity.model.Consignor;
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
}
