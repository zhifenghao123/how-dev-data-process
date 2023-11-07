package com.howdev.biz.po;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * user_order
 * @author 
 */
@Data
public class UserOrder implements Serializable {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 订单id
     */
    private Long orderId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 订单编号
     */
    private String orderNumber;

    /**
     * 订单总价
     */
    private BigDecimal totalPrice;

    /**
     * 状态，1-待付款，2-待发货，3-已发货，4-已签收，5-已取消
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date created;

    /**
     * 更新时间
     */
    private Date updated;

    private static final long serialVersionUID = 1L;
}