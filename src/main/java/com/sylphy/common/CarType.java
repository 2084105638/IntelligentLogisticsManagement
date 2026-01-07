package com.sylphy.common;

/**
 * @author apple
 * @since 2026/1/7 16:30
 */
public enum CarType {
    /**
     * 车辆类型
     */
    MICRO(0, "微型"),
    LIGHT(1, "轻型"),
    MEDIUM(2, "中型"),
    HEAVY(3, "重型");
    
    private final Integer code;
    private final String desc;
    
    CarType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
