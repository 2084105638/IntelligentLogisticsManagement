package com.sylphy.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 运单状态枚举
 *
 * @author apple
 * @since 2026/1/4
 */
@Getter
@AllArgsConstructor
public enum WaybillStatus {

    PENDING_ALLOCATION(0, "待分配"),
    ALLOCATED(1, "已分配"),
    IN_TRANSIT(2, "运输中"),
    COMPLETED(3, "已完成");

    private final Integer code;
    private final String desc;

    public static String getDesc(Integer code) {
        if (code == null) {
            return "";
        }
        for (WaybillStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return "";
    }
}
