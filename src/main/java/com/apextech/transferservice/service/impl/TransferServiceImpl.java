package com.apextech.transferservice.service.impl;

import com.apextech.transferservice.constant.TransferStatusEnum;
import com.apextech.transferservice.dto.request.TransferRequest;
import com.apextech.transferservice.dto.response.SessionData;
import com.apextech.transferservice.dto.response.SessionResponse;
import com.apextech.transferservice.dto.response.TransferResponse;
import com.apextech.transferservice.entity.TransferEntity;
import com.apextech.transferservice.repository.BankAccountRepository;
import com.apextech.transferservice.repository.TransferRepository;
import com.apextech.transferservice.service.SessionService;
import com.apextech.transferservice.service.TransferService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {
    private static final BigDecimal USD_TO_KHR = new BigDecimal("4000.0000");
    private static final BigDecimal KHR_TO_USD = new BigDecimal("0.00025");

    private final BankAccountRepository bankAccountRepository;
    private final TransferRepository transferRepository;
    private final SessionService sessionService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public SessionResponse transferValidate(TransferRequest request) {

        String fromAccount = request.getFromAccount();
        String toAccount = request.getToAccount();

        //validate from account
        boolean isValidFromAccount = bankAccountRepository.existsByAccountNumber(fromAccount);

        if(!isValidFromAccount){
            throw new IllegalArgumentException(
                    "From account not found."
            );
        }

        //validate to account
        boolean isValidToAccount = bankAccountRepository.existsByAccountNumber(toAccount);
        if(!isValidToAccount){
            throw new IllegalArgumentException(
                    "To account not found."
            );
        }

        String fromCurrency = normalizeCurrency(request.getFromCurrency());
        String toCurrency = normalizeCurrency(request.getToCurrency());

        BigDecimal txnAmount = request.getTxnAmount();

        // 2. Validate amount
        if (txnAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transaction amount must be greater than zero"
            );
        }

        // 3. Check cross-currency transfer
        boolean isCrossTransfer = isCrossCurrency(fromCurrency, toCurrency);

        // 4. Determine exchange rate
        BigDecimal rate = getExchangeRate(fromCurrency, toCurrency);

        // 5. Calculate receiver amount
        BigDecimal receiverAmount;

        if (isCrossTransfer) {
            // Convert sender amount to receiver's currency
            receiverAmount = txnAmount
                    .multiply(rate)
                    .setScale(4, RoundingMode.HALF_UP);
        } else {
            // Same currency: no conversion
            receiverAmount = txnAmount;
        }

        SessionData sessionData = SessionData.builder()
                .build();

        sessionData.put("fromAccount", fromAccount);
        sessionData.put("toAccount", toAccount);
        sessionData.put("fromCurrency",fromCurrency);
        sessionData.put("toCurrency", toCurrency);
        sessionData.put("txnAmount", txnAmount);
        sessionData.put("receiverAmount", receiverAmount);
        sessionData.put("fee", 0);
        sessionData.put("rate", rate);

        return sessionService.create(sessionData);
    }

    @Override
    public TransferResponse transferCommit(String sessionId) {
        //Verify session id
        boolean isValidSessionId = sessionService.verify(sessionId);

        //Get session data
        if(isValidSessionId){
            SessionData sessionData = sessionService.getSession(sessionId);
            if (sessionData != null) {
                TransferEntity transfer = TransferEntity.builder()
                        .fromAccount((String) sessionData.get("fromAccount"))
                        .toAccount((String) sessionData.get("toAccount"))
                        .txnAmount((BigDecimal) sessionData.get("txnAmount"))
                        .totalAmount((BigDecimal) sessionData.get("receiverAmount"))
                        .fee((BigDecimal) sessionData.get("fee"))
                        .rate((BigDecimal) sessionData.get("rate"))
                        .fromCurrency((String) sessionData.get("fromCurrency"))
                        .toCurrency((String) sessionData.get("toCurrency"))
                        .status(TransferStatusEnum.SUCCESS.toString())
                        .createdBy((String) sessionData.get("fromAccount"))
                        .modifiedBy((String) sessionData.get("fromAccount"))
                        .build();

                TransferEntity savedTransfer = transferRepository.save(transfer);
                entityManager.refresh(savedTransfer);

                String transactionId = savedTransfer.getTransactionId();

                return TransferResponse.builder()
                        .transactionId(transactionId)
                        .fromAccount((String) sessionData.get("fromAccount"))
                        .toAccount((String) sessionData.get("toAccount"))
                        .amount((String) sessionData.get("receiverAmount"))
                        .currency((String) sessionData.get("toCurrency"))
                        .build();
            }
        }
        return null;
    }

    private boolean isCrossCurrency(String fromCurrency, String toCurrency) {
        return !normalizeCurrency(fromCurrency)
                .equals(normalizeCurrency(toCurrency));
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }

        String normalized = currency.trim().toUpperCase();

        if (!normalized.equals("USD") && !normalized.equals("KHR")) {
            throw new IllegalArgumentException(
                    "Only USD and KHR currencies are supported"
            );
        }

        return normalized;
    }

    private BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        String from = normalizeCurrency(fromCurrency);
        String to = normalizeCurrency(toCurrency);

        if (from.equals(to)) {
            return BigDecimal.ONE;
        }

        if ("USD".equals(from) && "KHR".equals(to)) {
            return USD_TO_KHR;
        }

        if ("KHR".equals(from) && "USD".equals(to)) {
            return KHR_TO_USD;
        }

        throw new IllegalArgumentException("Unsupported currency conversion");
    }

}
