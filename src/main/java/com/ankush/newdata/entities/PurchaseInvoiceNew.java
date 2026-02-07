package com.ankush.newdata.entities;

import com.ankush.data.entities.Bank;
import com.ankush.data.entities.Login;
import com.ankush.data.entities.PurchaseParty;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Table(name = "purchaseinvoicenew")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class PurchaseInvoiceNew {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String partyInvoiceNo;

    @ManyToOne
    @JoinColumn(name = "purchaseparty_id")
    private PurchaseParty purchaseParty;

    private LocalDate date;
    private float netTotal;
    private float transporting;
    private float wages;
    private float other;
    private float discount;
    private float grandTotal;
    private float paid;

    @ManyToOne
    @JoinColumn(name = "bank_id")
    private Bank bank;

    @ManyToOne
    @JoinColumn(name = "login_id")
    private Login login;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PurchaseTransactionNew> transactions = new ArrayList<>();

    public void addTransaction(PurchaseTransactionNew transaction) {
        transactions.add(transaction);
        transaction.setInvoice(this);
    }
}
