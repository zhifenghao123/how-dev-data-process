package com.howdev.web.controller;

import com.howdev.web.model.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class DepositController {


    @PostMapping("/batchDeposit")
    public BaseResponse<BatchDepositResponse> batchDeposit(@NotNull(message = "request is null") @Valid @RequestBody BaseRequest<BatchDepositRequest> request) {
        BatchDepositRequest batchDepositRequest = request.getData();
        String processNo = batchDepositRequest.getProcessBatchNo();
        List<DepositRequest> depositRequests = batchDepositRequest.getDepositRequests();

        // 检查重复userId
        Set<String> userIds = new HashSet<>();
        for (DepositRequest depositRequest : depositRequests) {
            if (!userIds.add(depositRequest.getUserId())) {
                return BaseResponse.newFailResponse("ParamError" ,"Duplicate userId found: " + depositRequest.getUserId());
            }
        }

        System.out.println(processNo);
        Set<String> succeededUserIds = new HashSet<>();
        Set<String> failedUserIds = new HashSet<>();
        for (int i = 0; i < depositRequests.size(); i++) {
            DepositRequest depositRequest = depositRequests.get(i);
            System.out.println(depositRequest.getUserId());
            System.out.println(depositRequest.getAmount());
            if (i % 2 == 0) {
                succeededUserIds.add(depositRequest.getUserId());
            } else {
                failedUserIds.add(depositRequest.getUserId());
            }
        }

        BatchDepositResponse result = new BatchDepositResponse();
        result.setSucceededUserIds(succeededUserIds);
        result.setFailedUserIds(failedUserIds);
        return BaseResponse.newSuccResponse(result);
    }
}
