package com.apextech.transferservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferSessionResponse {
    private String fromAccount;
    private String toAccount;

    private BigDecimal txnAmount;
    private BigDecimal totalAmount;
    private BigDecimal fee;
    private BigDecimal rate;

    private String fromCurrency;
    private String toCurrency;
    private String serviceType;
}
