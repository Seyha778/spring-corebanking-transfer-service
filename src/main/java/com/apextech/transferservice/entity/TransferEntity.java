package com.apextech.transferservice.entity;

import com.apextech.transferservice.constant.TransferServiceEnum;
import com.apextech.transferservice.constant.TransferStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "transfers",
        schema = "dgtl_backend",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transfer_transaction_id",
                        columnNames = "transaction_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "transaction_id",
            nullable = false,
            unique = true,
            insertable = false,
            updatable = false,
            length = 40
    )
    private String transactionId;

    @Column(name = "from_account", nullable = false)
    private String fromAccount;

    @Column(name = "to_account", nullable = false)
    private String toAccount;

    @Column(name = "txn_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal txnAmount;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Builder.Default
    @Column(name = "fee", nullable = false, precision = 19, scale = 4)
    private BigDecimal fee = BigDecimal.valueOf(0);

    @Builder.Default
    @Column(name = "rate", nullable = false, precision = 19, scale = 4)
    private BigDecimal rate = BigDecimal.valueOf(0);

    @Builder.Default
    @Column(name = "from_currency", nullable = false, length = 3)
    private String fromCurrency = "USD";

    @Builder.Default
    @Column(name = "to_currency", nullable = false, length = 3)
    private String toCurrency = "USD";

    @Column(name = "description")
    private String description;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private String status = TransferStatusEnum.PENDING.toString();

    @Builder.Default
    @Column(name = "service", nullable = false)
    private String service = TransferServiceEnum.OTHER.toString();

    @Column(name = "created_by")
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_on", nullable = false, updatable = false)
    private OffsetDateTime createdOn;

    @Column(name = "modified_by")
    private String modifiedBy;

    @UpdateTimestamp
    @Column(name = "modified_on")
    private OffsetDateTime modifiedOn;
}