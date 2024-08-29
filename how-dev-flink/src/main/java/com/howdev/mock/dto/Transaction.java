package com.howdev.mock.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;

@Data
public class Transaction {
    public static final long[] USER_ID_RANGE = {1000L, 9999L};
    public static final double INIT_BALANCE = 100000.0;
    public static final String[] EXPENSES_TYPE_ENUM = {"娱乐", "交通", "学习", "网购", "向他人转账", "为其他App充值"};
    public static final String[] INCOME_TYPE_ENUM = {"工资", "兼职", "理财生息", "他人转账", "其他App提现"};

    public static final Map<String, String> EXPENSES_TYPE_TO_AMOUNT_RANGE = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("娱乐", "1-200");
        put("交通", "1-300");
        put("学习", "10-1000");
        put("网购", "1-200");
        put("向他人转账", "1-100");
        put("为其他App充值", "1-100");
    }});
    public static final Map<String, String> INCOME_TYPE_TO_AMOUNT_RANGE = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("工资", "15000-20000");
        put("兼职", "500-1000");
        put("理财生息", "100-500");
        put("他人转账", "100-1000");
        put("其他App提现", "20-200");

    }});
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
    private String amount;
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
    /**
     * 交易发生后账户余额
     */
    private String balance;

}
