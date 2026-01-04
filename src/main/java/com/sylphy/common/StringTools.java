package com.sylphy.common;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

/**
 * Token 工具类
 *
 * @author apple
 * @since 2026/1/3
 */
public class StringTools {
    
    /**
     * 生成 Token
     *
     * @return Token字符串
     */
    public static String generateToken() {
        return IdUtil.simpleUUID();
    }
    
    public static String randomUsername() {
        return RandomUtil.randomString("user", 10);
    }
}
