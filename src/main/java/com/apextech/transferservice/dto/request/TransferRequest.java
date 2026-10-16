package com.apextech.transferservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class TransferRequest {

    @JsonProperty("from_account")
    @NotBlank
    private String fromAccount;

    @JsonProperty("to_account")
    @NotBlank
    private String toAccount;

    @JsonProperty("from_currency")
    @NotBlank
    private String fromCurrency;

    @JsonProperty("to_currency")
    @NotBlank
    private String toCurrency;

    @JsonProperty("txn_amount")
    @NotBlank
    private BigDecimal txnAmount;

}
