package com.apextech.transferservice.entity;

import com.apextech.transferservice.constant.AccountStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "bank_accounts",
        schema = "dgtl_backend",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bank_account_number",
                columnNames = "account_number"
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true, length = 32)
    private String accountNumber;

    @Column(name = "cif", nullable = false)
    private String cif;

    @Column(name = "branch_code", nullable = false)
    private String branchCode;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = AccountStatusEnum.PROGRESS.toString();

    @CreationTimestamp
    @Column(name = "created_on", nullable = false, updatable = false)
    private OffsetDateTime createdOn;

    @Column(name = "created_by")
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;

    @Column(name = "modified_by")
    private String modifiedBy;
}
