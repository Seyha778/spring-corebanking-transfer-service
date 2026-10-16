package com.apextech.transferservice.repository;

import com.apextech.transferservice.entity.BankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccountEntity, Long> {

    boolean existsByAccountNumber(String accountNumber);
}