package generate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * sys_waybill
 */
@Data
public class Waybill implements Serializable {
    /**
     * 运单主键 id
     */
    private Long waybillId;

    /**
     * 货物信息
     */
    private String goodsInformation;

    /**
     * 起始地址
     */
    private String startAddress;

    /**
     * 结束地址
     */
    private String endAddress;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 期望时效
     */
    private Date expectedTimeLimit;

    /**
     * 费用
     */
    private BigDecimal cost;

    /**
     * 状态 待分配:0 已分配:1 运输中: 2 已完成:3
     */
    private Boolean status;

    private static final long serialVersionUID = 1L;
}