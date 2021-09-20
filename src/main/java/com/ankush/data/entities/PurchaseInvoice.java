package com.ankush.data.entities;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Table(name = "purchaseinvoice")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class PurchaseInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String partyinvoiceno;
    @ManyToOne
    @JoinColumn(name = "purchaseparty_id")
    private PurchaseParty purchaseParty;
    private LocalDate date;
    private float nettotal;
    private float transporting;
    private float wages;
    private float other;
    private float discount;
    private float grandtotal;
    private float paid;
    @ManyToOne
    @JoinColumn(name = "bank_id")
    private Bank bank;
    @ManyToOne
    @JoinColumn(name = "login_id")
    private Login login;

    @OneToMany(mappedBy = "invoice",fetch = FetchType.EAGER)
    private List<PurchaseTransaction> transactions = new ArrayList<>();

    public List<PurchaseTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<PurchaseTransaction> transactions) {
        this.transactions = transactions;
    }
}
