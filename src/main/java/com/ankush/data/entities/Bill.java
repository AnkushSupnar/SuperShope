package com.ankush.data.entities;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Table(name = "bill")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "billno", nullable = false)
    private Long billno;

    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "bankid")
    private Bank bank;

    @ManyToOne(optional = false)
    @JoinColumn(name = "loginid", nullable = false)
    private Login login;

    @Column(name = "discount")
    private Float discount;

    @Column(name = "nettotal")
    private Float nettotal;

    @Column(name = "othercharges")
    private Float othercharges;

    @Column(name = "grandtotal")
    private Float grandtotal;

    @Column(name = "paid")
    private Float paid;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;

}