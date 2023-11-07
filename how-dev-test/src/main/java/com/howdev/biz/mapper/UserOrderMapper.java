package com.howdev.biz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.howdev.biz.po.UserOrder;

public interface UserOrderMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserOrder record);

    int insertSelective(UserOrder record);

    UserOrder selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserOrder record);

    int updateByPrimaryKey(UserOrder record);

    int batchInsert(@Param("userOrders") List<UserOrder> userOrders);
}