package com.howdev.biz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.howdev.biz.po.OrderDetail;

public interface OrderDetailMapper {
    int deleteByPrimaryKey(Long id);

    int insert(OrderDetail record);

    int insertSelective(OrderDetail record);

    OrderDetail selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(OrderDetail record);

    int updateByPrimaryKey(OrderDetail record);

    int batchInsert(@Param("orderDetails") List<OrderDetail> orderDetails);
}