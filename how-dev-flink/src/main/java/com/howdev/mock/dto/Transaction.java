package com.howdev.mock.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class Transaction {
    public static final long[] USER_ID_RANGE = {0L, 99999L};
    public static final double[] AMOUNT_RANGE = {-1000000.0, 1000000.0};
    public static final String[] TYPE_ENUM = {"餐饮", "购物", "娱乐", "交通", "学习", "住房", "其他"};
    public static final String[] LOCATION_ENUM= {"北京市", "上海市", "重庆市", "天津市", "广东省广州市", "广东省深圳市",
            "江苏省南京市", "江苏省苏州市", "浙江省杭州市", "浙江省宁波市", "安徽省合肥市", "山东省济南市", "山东省青岛市",
            "河南省郑州市", "河南省洛阳市", "湖北省武汉市", "湖南省长沙市", "四川省成都市", "福建省福州市", "江西省南昌市", "陕西省西安市", "陕西省宝鸡市","辽宁省沈阳市"};

    public static final LocalDateTime START_TIME = LocalDateTime.of(2024, 7, 29, 0, 0, 0);
    public static final LocalDateTime END_TIME = LocalDateTime.of(2024, 8, 6, 23, 59, 59);

    /**
     * id
     */
    private long id;

    /**
     * 用户ID
     */
    private long userId;
    /**
     * 交易金额，单位：元
     * 正数代表收入，负数代表支出
     */
    private double amount;
    /**
     * 交易类别
     */
    private String type;
    /**
     * 交易发生地点
     */
    private String occurredLocation;
    /**
     * 交易发生时间
     */
    private Date occurredTime;

}
