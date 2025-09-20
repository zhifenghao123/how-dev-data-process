package com.howdev.web.model;

import lombok.Data;

import java.util.Set;

@Data
public class BatchDepositResponse {
    private Set<String> succeededUserIds;
    private Set<String> failedUserIds;
}
