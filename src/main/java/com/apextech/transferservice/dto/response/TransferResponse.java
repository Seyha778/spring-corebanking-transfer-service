package com.apextech.transferservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TransferResponse {

    private String transactionId;

    private String fromAccount;

    private String toAccount;

    private String currency;

    private String amount;
}