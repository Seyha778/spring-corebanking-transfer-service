package com.apextech.transferservice.service.impl;

import com.apextech.transferservice.constant.TransactionTypeEnum;
import com.apextech.transferservice.constant.TransferStatusEnum;
import com.apextech.transferservice.dto.request.SessionRequest;
import com.apextech.transferservice.dto.request.TransferRequest;
import com.apextech.transferservice.dto.response.SessionData;
import com.apextech.transferservice.dto.response.SessionResponse;
import com.apextech.transferservice.dto.response.TransferSessionResponse;
import com.apextech.transferservice.entity.BankAccountEntity;
import com.apextech.transferservice.entity.TransferEntity;
import com.apextech.transferservice.repository.BankAccountRepository;
import com.apextech.transferservice.repository.TransferRepository;
import com.apextech.transferservice.service.SessionService;
import com.apextech.transferservice.service.TransferService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

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

    private final ObjectMapper objectMapper;

    @Override
    public SessionResponse transferValidate(TransferRequest request) {

        String fromAccount = request.getFromAccount();
        String toAccount = request.getToAccount();

        //validate sender account
        BankAccountEntity senderInfo = bankAccountRepository
                .findTopByAccountNumber(fromAccount)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        TransactionTypeEnum.DEBIT.getDescription() + " account not found."
                ));

        //validate receiver account
        BankAccountEntity receiverInfo = bankAccountRepository
                .findTopByAccountNumber(fromAccount)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        TransactionTypeEnum.CREDIT.getDescription() + " account not found."
                ));

        // 2. Validate amount
        BigDecimal txnAmount = request.getTxnAmount();
        if (txnAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Transaction amount must be greater than zero"
            );
        }

        // validate account balance and get available account balance
        BigDecimal availableBalance = validateTxnBalanceAndGetFinalBalance(senderInfo.getBalance(), senderInfo.getBlockAmount());

        // validate txn balance and get final balance
        validateTxnBalanceAndGetFinalBalance(availableBalance, txnAmount);

        // 2.2 Currency checking
        String fromCurrency = normalizeCurrency(request.getFromCurrency());
        String toCurrency = normalizeCurrency(request.getToCurrency());

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

        TransferSessionResponse transferSession = TransferSessionResponse.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .txnAmount(txnAmount)
                .totalAmount(receiverAmount)
                .fee(BigDecimal.ZERO)
                .rate(rate)
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .serviceType(request.getServiceType())
                .build();

        sessionData.put("data", transferSession);

        return sessionService.create(sessionData);
    }

    @Override
    @Transactional
    public TransferEntity transferCommit(SessionRequest request) {
        String sessionId = request.getSessionId();

        //Get session data
        SessionData sessionData = sessionService.getSession(sessionId);
        if (sessionData != null) {
            TransferSessionResponse transferSession = objectMapper.convertValue(
                    sessionData.get("data"),
                    TransferSessionResponse.class
            );

            TransferEntity transfer = TransferEntity.builder()
                    .fromAccount(transferSession.getFromAccount())
                    .toAccount(transferSession.getToAccount())
                    .txnAmount(transferSession.getTxnAmount())
                    .totalAmount(transferSession.getTotalAmount())
                    .fee(transferSession.getFee())
                    .rate(transferSession.getRate())
                    .fromCurrency(transferSession.getFromCurrency())
                    .toCurrency(transferSession.getToCurrency())
                    .status(TransferStatusEnum.SUCCESS.toString())
                    .service(transferSession.getServiceType())
                    .createdBy(transferSession.getFromAccount())
                    .modifiedBy(transferSession.getFromAccount())
                    .build();

            TransferEntity res = recordTransfer(transfer);

            minusBalance(transferSession.getFromAccount(), res.getTxnAmount());
            plusBalance(transferSession.getToAccount(), res.getTotalAmount());
            return recordTransfer(transfer);
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

    public TransferEntity recordTransfer(TransferEntity transfer) {
        TransferEntity savedTransfer = transferRepository.saveAndFlush(transfer);
        entityManager.refresh(savedTransfer);
        return savedTransfer;
    }

    private void minusBalance(String accountNumber, BigDecimal balance) {
        bankAccountRepository.minusBalance(accountNumber, balance);
    }

    private void plusBalance(String accountNumber, BigDecimal balance) {
        bankAccountRepository.plusBalance(accountNumber, balance);
    }

    private BigDecimal validateTxnBalanceAndGetFinalBalance(BigDecimal balance, BigDecimal txnAmount) {
        BigDecimal finalBalance = Optional.ofNullable(balance)
                .orElse(BigDecimal.ZERO)
                .subtract(
                        Optional.ofNullable(txnAmount)
                                .orElse(BigDecimal.ZERO)
                );
        if (finalBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient account balance."
            );
        }
        return finalBalance;
    }

}
