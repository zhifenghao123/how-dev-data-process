package com.howdev.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionAggregateResult {
    private String occurredMinuteTimeText;
    private Long occurredMinuteTimeStamp;

    private Date latestOccurredTime;
    private Long latestOccurredTimeStamp;

    private Long userId;
    private String occurredLocation;

    private BigDecimal incomeAmount;
    private Integer incomeCount;
    private BigDecimal expensesAmount;
    private Integer expensesCount;

    private Integer totalCount;

    private BigDecimal balance;
}
