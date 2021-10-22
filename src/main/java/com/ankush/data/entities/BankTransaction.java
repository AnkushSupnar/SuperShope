package com.ankush.data.entities;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Table(name = "banktransaction")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class BankTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "particulars")
    private String particulars;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "debit")
    private Float debit;

    @Column(name = "credit")
    private Float credit;

    @Column(name = "transaction_id")
    private Long transactionid;

    @ManyToOne
    @JoinColumn(name = "bank_id")
    private Bank bank;
}