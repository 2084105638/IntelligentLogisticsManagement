package com.sylphy.common;

import cn.hutool.core.util.IdUtil;

/**
 * Token 工具类
 *
 * @author apple
 * @since 2026/1/3
 */
public class TokenUtil {

    /**
     * 生成 Token
     *
     * @return Token字符串
     */
    public static String generateToken() {
        return IdUtil.simpleUUID();
    }
}
