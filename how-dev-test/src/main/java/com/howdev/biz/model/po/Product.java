package com.howdev.biz.model.po;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * product
 * @author 
 */
@Data
public class Product implements Serializable {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品描述
     */
    private String description;

    /**
     * 产品单价
     */
    private BigDecimal price;

    /**
     * 分类id
     */
    private Long categoryId;

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