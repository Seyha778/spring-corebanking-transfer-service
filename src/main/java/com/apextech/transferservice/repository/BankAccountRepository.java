package com.apextech.transferservice.repository;

import com.apextech.transferservice.entity.BankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccountEntity, Long> {

    boolean existsByAccountNumber(String accountNumber);

    Optional<BankAccountEntity> findTopByAccountNumber(String accountNumber);

    @Modifying
    @Transactional
    @Query("""
    UPDATE BankAccountEntity a
    SET a.balance = a.balance - :balance
    WHERE a.accountNumber = :accountNumber
    """)
    void minusBalance(@Param("accountNumber") String accountNumber, BigDecimal balance);



    @Modifying
    @Transactional
    @Query("""
    UPDATE BankAccountEntity a
    SET a.balance = a.balance + :balance
    WHERE a.accountNumber = :accountNumber
    """)
    void plusBalance(@Param("accountNumber") String accountNumber, BigDecimal balance);
}