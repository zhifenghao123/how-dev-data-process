package com.howdev.biz.model.bo;

import java.math.BigDecimal;
import java.util.List;

import com.howdev.biz.model.po.OrderDetail;

import lombok.Data;

/**
 * UserOrderBO class
 *
 * @author haozhifeng
 * @date 2023/11/07
 */
@Data
public class UserOrderBO {
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
     * 订单详情集合
     */
    private List<OrderDetail> orderDetails;
}

