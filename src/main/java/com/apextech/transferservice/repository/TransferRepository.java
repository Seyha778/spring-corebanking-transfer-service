package com.apextech.transferservice.repository;

import com.apextech.transferservice.entity.TransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<TransferEntity, Long> {

    Optional<TransferEntity> findByTransactionId(String transactionId);

    List<TransferEntity> findByFromAccount(String fromAccount);

    List<TransferEntity> findByToAccount(String toAccount);

    List<TransferEntity> findByStatus(String status);

    List<TransferEntity> findByFromAccountAndStatus(
            String fromAccount,
            String status
    );

    List<TransferEntity> findByToAccountAndStatus(
            String toAccount,
            String status
    );

    boolean existsByTransactionId(String transactionId);
}