package com.howdev.biz.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * order_detail
 * @author 
 */
@Data
public class OrderDetail implements Serializable {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 订单详情id
     */
    private Long orderDetailId;

    /**
     * 订单id
     */
    private Long orderId;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 价格
     */
    private BigDecimal price;

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