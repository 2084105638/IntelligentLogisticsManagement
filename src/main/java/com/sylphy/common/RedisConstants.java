package com.sylphy.common;

/**
 * Redis Key 常量类
 *
 * @author apple
 * @since 2026/1/3
 */
public class RedisConstants {

    /**
     * Token 前缀
     */
    public static final String TOKEN_PREFIX = "token:";

    /**
     * 货主信息前缀
     */
    public static final String CONSIGNOR_INFO_PREFIX = "consignor:info:";

    /**
     * 用户信息前缀
     */
    public static final String USER_INFO_PREFIX = "user:info:";

    /**
     * Token 过期时间（秒）- 7天
     */
    public static final long TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60;

    /**
     * 用户信息缓存过期时间（秒）- 30分钟
     */
    public static final long USER_INFO_EXPIRE_TIME = 30 * 60;
}
