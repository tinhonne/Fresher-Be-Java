package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.example.demo.constant.ValidationConstants.*;

@Entity
@Table(
        name = "bank_transactions",
        indexes = {
                @Index(name = "idx_transaction_from_account_date", columnList = "from_account_id, transaction_date"),
                @Index(name = "idx_transaction_to_account_date", columnList = "to_account_id, transaction_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_account_id", nullable = false)
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_account_id", nullable = false)
    private Account toAccount;

    @Column(nullable = false, precision = MONEY_PRECISION, scale = MONEY_SCALE)
    private BigDecimal amount;

    @Convert(converter = TransactionStatusConverter.class)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column
    private String content;

    @Column
    private String errorReason;
}
